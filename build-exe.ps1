$ErrorActionPreference = "Stop"

Write-Host "开始清理并编译项目..."
mvn clean package

if ($LASTEXITCODE -ne 0 -and $LASTEXITCODE -ne $null) {
    Write-Host "Maven 编译失败！请确保安装了 Maven 并可在命令行中运行 mvn 命令。" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "开始使用 jpackage 打包成免安装的 EXE (app-image)..."
if (Test-Path "target\installer") {
    Remove-Item -Recurse -Force "target\installer"
}

# 使用 app-image 可以在不需要安装 WiX Toolset 的情况下生成带有 exe 启动器的独立运行目录
jpackage --type app-image `
    --name "SymlinkGUI" `
    --description "Windows 软链接创建工具" `
    --app-version "1.1" `
    --module-path "target\symlink-gui-1.1.jar;target\lib" `
    --module top.nodaoli/top.nodaoli.App `
    --dest target\installer

if ($LASTEXITCODE -eq 0 -or $LASTEXITCODE -eq $null) {
    Write-Host "打包成功！请打开目录查看: target\installer\SymlinkGUI" -ForegroundColor Green
}
else {
    Write-Host "打包失败，请检查 jpackage 错误日志。" -ForegroundColor Red
}
