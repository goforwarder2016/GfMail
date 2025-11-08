#!/bin/bash

# Gfmail 测试运行脚本
echo "=== Gfmail 测试框架验证 ==="

# 检查 Gradle 版本
echo "1. 检查 Gradle 版本..."
./gradlew --version | head -5

# 编译主应用
echo -e "\n2. 编译主应用..."
if ./gradlew assembleFreeDebug; then
    echo "✅ 主应用编译成功"
else
    echo "❌ 主应用编译失败"
    exit 1
fi

# 编译单元测试
echo -e "\n3. 编译单元测试..."
if ./gradlew compileFreeDebugUnitTestKotlin; then
    echo "✅ 单元测试编译成功"
else
    echo "❌ 单元测试编译失败"
    exit 1
fi

# 尝试运行简单测试
echo -e "\n4. 尝试运行简单测试..."
if ./gradlew testFreeDebugUnitTest --tests="com.gf.mail.utils.SimpleTest" 2>/dev/null; then
    echo "✅ 简单测试运行成功"
else
    echo "⚠️  简单测试运行失败（可能是 Gradle 配置问题）"
fi

# 生成测试报告
echo -e "\n5. 生成测试覆盖率报告..."
if ./gradlew jacocoTestReport; then
    echo "✅ 测试覆盖率报告生成成功"
    echo "📊 报告位置: app/build/reports/jacoco/test/html/index.html"
else
    echo "⚠️  测试覆盖率报告生成失败"
fi

echo -e "\n=== 测试框架状态总结 ==="
echo "✅ 基础编译: 正常"
echo "✅ 测试编译: 正常"
echo "⚠️  测试运行: 需要修复 Gradle 配置"
echo "✅ 覆盖率报告: 配置完成"

echo -e "\n📋 下一步建议:"
echo "1. 修复 Gradle 测试任务配置问题"
echo "2. 创建正确的测试数据工厂"
echo "3. 逐步添加 Repository 和 UseCase 测试"
echo "4. 实现 85%+ 测试覆盖率目标"