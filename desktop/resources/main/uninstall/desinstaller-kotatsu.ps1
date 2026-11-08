# =============================================================================
# Désinstallateur personnalisé de Kotatsu Desktop (portage Windows de Kotatsu-Redo)
# GPLv3 — ce script fait partie du travail dérivé.
#
# Rôle :
#   1. Fermer l'application si elle est lancée.
#   2. Désinstaller proprement le produit via Windows Installer (msiexec).
#   3. DEMANDER à l'utilisateur s'il souhaite CONSERVER ses données utilisateur
#      (favoris, historique, téléchargements, préférences — %APPDATA%\Kotatsu)
#      pour une éventuelle installation ultérieure.
#
# Emplacement dans l'app installée : <installation>\app\resources\uninstall\
# =============================================================================

$ErrorActionPreference = "Continue"

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "  Desinstallation de Kotatsu Desktop"          -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

# --- 1. Fermer l'application si elle tourne ---------------------------------
$procs = Get-Process -Name "Kotatsu", "Kotatsu Desktop" -ErrorAction SilentlyContinue
if ($procs) {
    Write-Host "Fermeture de Kotatsu Desktop..." -ForegroundColor Yellow
    $procs | Stop-Process -Force
    Start-Sleep -Seconds 2
}

# --- 2. Désinstaller le produit (Windows Installer) -------------------------
# Recherche dans les clés de désinstallation (machine + utilisateur) : les
# installateurs « Kotatsu Desktop » (1.x) et les anciens « Kotatsu » (9.8.6).
$keys = @(
    "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\*",
    "HKLM:\SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\*",
    "HKCU:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\*"
) | ForEach-Object { Get-ItemProperty $_ -ErrorAction SilentlyContinue } |
    Where-Object { $_.DisplayName -like "Kotatsu Desktop*" -or $_.DisplayName -like "Kotatsu 9.8.6*" }

if (-not $keys) {
    Write-Host "Aucun produit Kotatsu Desktop installe trouve (deja desinstalle ?)." -ForegroundColor Yellow
} else {
    foreach ($entry in $keys) {
        $name = $entry.DisplayName
        $version = $entry.DisplayVersion
        Write-Host "Desinstallation de '$name' ($version)..." -ForegroundColor Yellow
        if ($entry.PSChildName -match "^\{[0-9A-Fa-f-]+\}$") {
            # Produit Windows Installer : desinstallation silencieuse par code produit
            Start-Process -FilePath "msiexec.exe" -ArgumentList "/x `"$($entry.PSChildName)`" /qn" -Wait
        } elseif ($entry.QuietUninstallString) {
            Start-Process -FilePath "cmd.exe" -ArgumentList "/c $($entry.QuietUninstallString)" -Wait
        } elseif ($entry.UninstallString) {
            Start-Process -FilePath "cmd.exe" -ArgumentList "/c $($entry.UninstallString) /quiet" -Wait
        }
        Write-Host "  -> termine." -ForegroundColor Green
    }
    Start-Sleep -Seconds 2
}

# --- 3. Choix : conserver ou non les données utilisateur ---------------------
Write-Host ""
Write-Host "Vos donnees personnelles (favoris, historique, telechargements,"  -ForegroundColor White
Write-Host "preferences) sont conservees dans : $env:APPDATA\Kotatsu"           -ForegroundColor White
Write-Host ""
$answer = Read-Host "Conserver ces donnees pour une eventuelle reinstallation ? [O/n]"
$answer = $answer.Trim().ToLower()

if ($answer -eq "" -or $answer -eq "o" -or $answer -eq "oui" -or $answer -eq "y" -or $answer -eq "yes") {
    Write-Host ""
    Write-Host "Donnees conservees dans $env:APPDATA\Kotatsu ." -ForegroundColor Green
    Write-Host "Une future installation de Kotatsu Desktop les retrouvera automatiquement."
} else {
    Write-Host ""
    Write-Host "Suppression des donnees utilisateur..." -ForegroundColor Yellow
    foreach ($dir in @("$env:APPDATA\Kotatsu", "$env:APPDATA\KotatsuRedo")) {
        if (Test-Path $dir) {
            Remove-Item -Path $dir -Recurse -Force -ErrorAction SilentlyContinue
            Write-Host "  $dir supprime." -ForegroundColor Green
        }
    }
    Write-Host "Donnees utilisateur supprimees." -ForegroundColor Green
}

Write-Host ""
Write-Host "Desinstallation terminee. Merci d'avoir utilise Kotatsu Desktop !" -ForegroundColor Cyan
Start-Sleep -Seconds 2
