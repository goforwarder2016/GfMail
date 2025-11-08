package com.gf.mail.data.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.gf.mail.domain.model.Email
import com.gf.mail.domain.model.EmailFolder
import com.gf.mail.domain.model.Account
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for handling offline mode and network connectivity
 */
@Singleton
class OfflineManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "OfflineManager"
    }
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    // Network state
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private val _networkType = MutableStateFlow(NetworkType.UNKNOWN)
    val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()
    
    // Offline queue for pending operations
    private val _pendingOperations = MutableStateFlow<List<OfflineOperation>>(emptyList())
    val pendingOperations: StateFlow<List<OfflineOperation>> = _pendingOperations.asStateFlow()
    
    // Events channel for network state changes
    private val _networkEvents = Channel<NetworkEvent>(Channel.UNLIMITED)
    val networkEvents = _networkEvents.receiveAsFlow()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network available: $network")
            _isOnline.value = true
            _networkType.value = getNetworkType(network)
            _networkEvents.trySend(NetworkEvent.Connected(getNetworkType(network)))
        }
        
        override fun onLost(network: Network) {
            Log.d(TAG, "Network lost: $network")
            _isOnline.value = false
            _networkType.value = NetworkType.NONE
            _networkEvents.trySend(NetworkEvent.Disconnected)
        }
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            val newType = getNetworkType(network)
            if (_networkType.value != newType) {
                _networkType.value = newType
                _networkEvents.trySend(NetworkEvent.TypeChanged(newType))
            }
        }
    }
    
    init {
        registerNetworkCallback()
        updateInitialNetworkState()
    }
    
    /**
     * Register network callback to monitor connectivity changes
     */
    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    /**
     * Update initial network state
     */
    private fun updateInitialNetworkState() {
        val activeNetwork = connectivityManager.activeNetwork
        if (activeNetwork != null) {
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                _isOnline.value = true
                _networkType.value = getNetworkType(activeNetwork)
            } else {
                _isOnline.value = false
                _networkType.value = NetworkType.NONE
            }
        } else {
            _isOnline.value = false
            _networkType.value = NetworkType.NONE
        }
    }
    
    /**
     * Get network type from network capabilities
     */
    private fun getNetworkType(network: Network): NetworkType {
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
            else -> NetworkType.UNKNOWN
        }
    }
    
    /**
     * Add operation to offline queue
     */
    fun addOfflineOperation(operation: OfflineOperation) {
        val currentOperations = _pendingOperations.value.toMutableList()
        currentOperations.add(operation)
        _pendingOperations.value = currentOperations
        Log.d(TAG, "Added offline operation: ${operation.type}")
    }
    
    /**
     * Remove operation from offline queue
     */
    fun removeOfflineOperation(operationId: String) {
        val currentOperations = _pendingOperations.value.toMutableList()
        currentOperations.removeAll { it.id == operationId }
        _pendingOperations.value = currentOperations
        Log.d(TAG, "Removed offline operation: $operationId")
    }
    
    /**
     * Clear all pending operations
     */
    fun clearPendingOperations() {
        _pendingOperations.value = emptyList()
        Log.d(TAG, "Cleared all pending operations")
    }
    
    /**
     * Get pending operations count
     */
    fun getPendingOperationsCount(): Int {
        return _pendingOperations.value.size
    }
    
    /**
     * Check if device is connected to internet
     */
    fun isConnectedToInternet(): Boolean {
        return _isOnline.value
    }
    
    /**
     * Check if device is on WiFi
     */
    fun isOnWiFi(): Boolean {
        return _networkType.value == NetworkType.WIFI
    }
    
    /**
     * Check if device is on cellular data
     */
    fun isOnCellular(): Boolean {
        return _networkType.value == NetworkType.CELLULAR
    }
    
    /**
     * Get network quality based on type
     */
    fun getNetworkQuality(): NetworkQuality {
        return when (_networkType.value) {
            NetworkType.WIFI -> NetworkQuality.EXCELLENT
            NetworkType.ETHERNET -> NetworkQuality.EXCELLENT
            NetworkType.CELLULAR -> NetworkQuality.GOOD
            NetworkType.UNKNOWN -> NetworkQuality.POOR
            NetworkType.NONE -> NetworkQuality.NONE
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        _networkEvents.close()
    }
}

/**
 * Network types
 */
enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    UNKNOWN,
    NONE
}

/**
 * Network quality levels
 */
enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    POOR,
    NONE,
    UNKNOWN
}

/**
 * Network events
 */
sealed class NetworkEvent {
    data class Connected(val networkType: NetworkType) : NetworkEvent()
    object Disconnected : NetworkEvent()
    data class TypeChanged(val networkType: NetworkType) : NetworkEvent()
}

/**
 * Offline operation types
 */
enum class OfflineOperationType {
    SEND_EMAIL,
    DELETE_EMAIL,
    MARK_AS_READ,
    MARK_AS_UNREAD,
    STAR_EMAIL,
    MOVE_EMAIL,
    CREATE_FOLDER,
    DELETE_FOLDER
}

/**
 * Offline operation data
 */
data class OfflineOperation(
    val id: String,
    val type: OfflineOperationType,
    val accountId: String,
    val data: Map<String, Any>,
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)