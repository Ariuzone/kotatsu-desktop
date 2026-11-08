# =============================================================================
# Installateur personnalise de Kotatsu Desktop (portage Windows de Kotatsu-Redo)
# GPLv3 — ce script fait partie du travail derive.
#
# Role :
#   1. Trouver le package a installer (fichier .msi a cote du script, ou -MsiPath).
#   2. Fermer l'application si elle tourne (les donnees ne sont jamais touches).
#   3. Gerer proprement les cas :
#        - Kotatsu Desktop 1.x deja installe  -> MISE A JOUR (major-upgrade MSI,
#          donnees conservees, a partir de la version installee).
#        - Ancien « Kotatsu » 9.8.6 installe   -> proposition de le desinstaller
#          d'abord (donnees conservees par defaut) pour eviter le conflit de
#          lignes produits, puis installation.
#        - Aucune installation                -> NOUVELLE INSTALLATION (assistant
#          graphique : choix du dossier, raccourcis).
#
# Usage :
#   .\installateur-kotatsu.ps1                    (cherche le .msi a cote du script)
#   .\installateur-kotatsu.ps1 -MsiPath <chemin>  (package explicite)
#   .\installateur-kotatsu.ps1 -Silent            (installation sans assistant)
# =============================================================================

param(
    [string]$MsiPath = "",
    [switch]$Silent
)

$ErrorActionPreference = "Continue"

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "  Installateur de Kotatsu Desktop"             -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

# --- 1. Localiser le package MSI --------------------------------------------
if (-not $MsiPath) {
    $here = Split-Path -Parent $MyInvocation.MyCommand.Path
    $candidates = Get-ChildItem -Path $here -Filter "Kotatsu Desktop*.msi" -ErrorAction SilentlyContinue |
                  Sort-Object Name -Descending
    if (-not $candidates) {
        Write-Host "Aucun package 'Kotatsu Desktop*.msi' trouve a cote de ce script." -ForegroundColor Red
        Write-Host "Placez le .msi dans le meme dossier que ce script, ou passez -MsiPath <chemin>."
        Read-Host "Entree pour fermer"
        exit 1
    }
    $MsiPath = $candidates[0].FullName
}
if (-not (Test-Path $MsiPath)) {
    Write-Host "Package introuvable : $MsiPath" -ForegroundColor Red
    Read-Host "Entree pour fermer"
    exit 1
}

# Version du package (nom de fichier ou propriete MSI)
$pkgVersion = ""
if ($MsiPath -match "(\d+\.\d+(\.\d+)?)") { $pkgVersion = $Matches[1] }
Write-Host "Package : $MsiPath" -ForegroundColor White
if ($pkgVersion) { Write-Host "Version : $pkgVersion" -ForegroundColor White }
Write-Host ""

# --- 2. Fermer l'application (sans toucher aux donnees) ----------------------
$procs = Get-Process -Name "Kotatsu", "Kotatsu Desktop" -ErrorAction SilentlyContinue
if ($procs) {
    Write-Host "Fermeture de Kotatsu Desktop (vos donnees ne sont pas touchees)..." -ForegroundColor Yellow
    $procs | Stop-Process -Force
    Start-Sleep -Seconds 2
}

# --- 3. Etat des installations existantes ------------------------------------
$keys = @(
    "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\*",
    "HKLM:\SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\*",
    "HKCU:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\*"
) | ForEach-Object { Get-ItemProperty $_ -ErrorAction SilentlyContinue }

$installed = $keys | Where-Object { $_.DisplayName -like "Kotatsu Desktop*" }
$legacy    = $keys | Where-Object { $_.DisplayName -like "Kotatsu 9.8.6*" }

# --- 4a. Ancienne ligne produit 9.8.6 : conflit de lignes produits -----------
foreach ($old in $legacy) {
    Write-Host "Ancienne installation detectee : '$($old.DisplayName)'" -ForegroundColor Yellow
    Write-Host "(ligne produit differente : la mise a jour directe est impossible, il faut la desinstaller)."
    $do = Read-Host "La desinstaller maintenant ? Vos donnees seront conservees. [O/n]"
    if ($do.Trim().ToLower() -ne "n") {
        Write-Host "Desinstallation de l'ancienne version..." -ForegroundColor Yellow
        if ($old.PSChildName -match "^\{[0-9A-Fa-f-]+\}$") {
            Start-Process -FilePath "msiexec.exe" -ArgumentList "/x `"$($old.PSChildName)`" /qn" -Wait
        } elseif ($old.QuietUninstallString) {
            Start-Process -FilePath "cmd.exe" -ArgumentList "/c $($old.QuietUninstallString)" -Wait
        }
        Write-Host "  -> termine (donnees conservees)." -ForegroundColor Green
    } else {
        Write-Host "Ancienne version conservee : la nouvelle installation peut continuer cote a cote." -ForegroundColor Yellow
    }
}

# --- 4b. Installation ou mise a jour -----------------------------------------
if ($installed) {
    $cur = $installed | Select-Object -First 1
    Write-Host ""
    Write-Host "Installation existante : '$($cur.DisplayName)' version $($cur.DisplayVersion)." -ForegroundColor White
    Write-Host "Mode : MISE A JOUR (major-upgrade MSI - favoris, historique et preferences conserves)." -ForegroundColor Green
    $args = "/i `"$MsiPath`" /qn /norestart"
    if (-not $Silent) { $args = "/i `"$MsiPath`" /norestart" }
} else {
    Write-Host ""
    Write-Host "Aucune installation existante. Mode : NOUVELLE INSTALLATION." -ForegroundColor Green
    if ($Silent) { $args = "/i `"$MsiPath`" /qn /norestart" }
    else { $args = "/i `"$MsiPath`" /norestart" }
}

# --- 5. Lancer l'installation (eleve pour l'ecriture dans Program Files) -----
Write-Host ""
if ($Silent) {
    Write-Host "Installation silencieuse en cours..." -ForegroundColor Yellow
    $proc = Start-Process -FilePath "msiexec.exe" -ArgumentList $args -Wait -PassThru -Verb RunAs
} else {
    Write-Host "Lancement de l'assistant d'installation (suivez les etapes a l'ecran)..." -ForegroundColor Yellow
    $proc = Start-Process -FilePath "msiexec.exe" -ArgumentList $args -Wait -PassThru -Verb RunAs
}

if ($proc.ExitCode -eq 0) {
    Write-Host ""
    Write-Host "Installation terminee avec succes !" -ForegroundColor Green
    Write-Host "Vos donnees (favoris, historique, telechargements) sont conservees dans $env:APPDATA\Kotatsu ."
} elseif ($proc.ExitCode -eq 3010) {
    Write-Host ""
    Write-Host "Installation terminee (redemarrage recommande)." -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "L'installation s'est terminee avec le code $($proc.ExitCode)." -ForegroundColor Yellow
    Write-Host "Si le code est 1602, l'operation a ete annulee."
}
Write-Host ""
Read-Host "Entree pour fermer"
