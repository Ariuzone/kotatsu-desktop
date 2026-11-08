@echo off
rem =============================================================================
rem Desinstallateur personnalise de Kotatsu Desktop (portage Windows, GPLv3).
rem Double-clic : desinstalle le produit puis demande si les donnees utilisateur
rem (%APPDATA%\Kotatsu) doivent etre conservees pour une reinstallation future.
rem =============================================================================
chcp 65001 >nul
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0desinstaller-kotatsu.ps1"
