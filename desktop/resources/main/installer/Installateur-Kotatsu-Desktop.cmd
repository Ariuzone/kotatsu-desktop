@echo off
rem =============================================================================
rem Installateur personnalise de Kotatsu Desktop (portage Windows, GPLv3).
rem Placez le package « Kotatsu Desktop-<version>.msi » a cote de ce script puis
rem double-cliquez : il gere l'installation neuve, la mise a jour (donnees
rem conservees) et la migration depuis l'ancienne ligne produit 9.8.6.
rem =============================================================================
chcp 65001 >nul
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0installateur-kotatsu.ps1"
