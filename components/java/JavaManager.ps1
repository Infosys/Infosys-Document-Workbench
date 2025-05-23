# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#


$JAVA_PROJECTS = @(
    @{projectName="DocWorkbenchUIWAR"; enabled=$true},
    @{projectName="DocWorkbenchServiceWAR"; enabled=$true},
    @{projectName="DocWorkbenchRulesWAR"; enabled=$true},
    @{projectName="DocWorkbenchEngine1WAR"; enabled=$true},
    @{projectName="DocWorkbenchEngine2WAR"; enabled=$true}
)
$CURRENT_DIRECTORY = (Get-Location).Path

### >>> Private Functions
function ExecuteCommand() {
    param (
        $commandArr,
        $outputFilePath
    )
    $output = @()
    Write-Debug "outputFilePath=$outputFilePath"
    foreach ($command in $commandArr) {
        Write-Host $command -ForegroundColor DarkCyan
        [ScriptBlock]$scriptBlock = [ScriptBlock]::Create($command) 
        if ($null -ne $outputFilePath) {
            Invoke-Command -ScriptBlock $scriptBlock | Tee-Object -FilePath $outputFilePath
        }
        else {
            Invoke-Command -ScriptBlock $scriptBlock 
        }
        # Invoke-Command -ScriptBlock $scriptBlock | Tee-Object -Variable varOutput
        # $output += $varOutput + [Environment]::NewLine
        $output += $varOutput
    }
    # return $output
}
function SetWindowsTitle {
    param (
        $title
    )
    $host.ui.RawUI.WindowTitle += ' - ' + $title
}
function BuildAngular() {
    Write-Host "BUILD JAVA PACKAGE - ANGULAR"
    $command = @()
    if (Test-Path "node_modules") {
        Write-Host "node_modules already present. Good!"
    }
    else {
        Write-Host "node_modules doesn't exist. Installing one time..."
        $command += "pnpm install"
    }

    $command += "npm run build"
    ExecuteCommand @($command)
}

function ShowWelcomeMessage {
    Write-Host ""                                                                       -ForegroundColor yellow
    Write-Host ""                                                                       -ForegroundColor yellow
    Write-Host "JAVA MANAGER"                                                         -ForegroundColor yellow
    Write-Host ""                                                                       -ForegroundColor yellow
    Write-Host "######################################################################" -ForegroundColor yellow
    Write-Host ""                                                                       -ForegroundColor yellow
    Write-Host "Menu-driven script to build java projects" -ForegroundColor yellow
    Write-Host ""                                                                     -ForegroundColor yellow
    Write-Host "######################################################################" -ForegroundColor yellow
    Write-Host ""
    Write-Host "Current Directory=$CURRENT_DIRECTORY" -BackgroundColor cyan -ForegroundColor black 
    Write-Host ""
}
function LaunchRecursiveMenu {
    do {
        Write-Host ""
        Write-Host "================ Select Option to Continue =========" -ForegroundColor yellow
        Write-Host ""
        Write-Host "   --------------- SELECT JAVA PROJECT ---------------------" -ForegroundColor Green
        # Local function
        function GetEnvNameMenu() {
            $map = @{}
            $ENVS = @(@("NA", "N/A", "A"), 
                @("test", "test environment", "B"), 
                @("dev", "dev environment", "C"))
            foreach ($env in $ENVS) {
                $menuIndex++
                Write-Host "   $($env[2]). $($env[1])" -ForegroundColor yellow
                $map += @{$env[2] = $env[0] }
            }            
            return $map
        }
        function GetJavaProjectMenu() {
            $map = @{}
            $menuIndex = 10
            foreach ($CONFIG_PROJECT in $JAVA_PROJECTS) {
                if (-not ($CONFIG_PROJECT.enabled)) {
                    continue 
                }
                $menuIndex++
                Write-Host "   $menuIndex`: $($CONFIG_PROJECT.projectName)" -ForegroundColor yellow
                $map += @{"$menuIndex" = $CONFIG_PROJECT.projectName }
            }
            return $map
        }
        $menuIndexJavaProjectNameMap = GetJavaProjectMenu 
        
        Write-Host ""
        Write-Host "   --------------- SELECT ENVIRONMENT ---------------------" -ForegroundColor Green
        $menuIndexEnvNameMap = GetEnvNameMenu
        # Write-Host ""
        # Write-Host "   --------------- GENERATE DEV REPORT ---------------------" -ForegroundColor Green
        # Write-Host "   51: DocWorkbenchDevReport" -ForegroundColor yellow
        Write-Host ""
        Write-Host "    Q: Quit" -ForegroundColor yellow
        Write-Host ""
        $userinput = Read-Host "Please make a selection. (E.g. 12B)"
        
        if ($userinput -match "^(\d+)([A-Za-z])$") {
            $numberPart = $matches[1]
            $charPart = $matches[2].ToUpper()            
        }
        
        switch ($userinput) {
            '51' {
                $command = "mvn -am -pl DocWorkbenchDevReport clean package -P client ""-Dmaven.test.failure.ignore=True""" 
                Invoke-Expression $command
                pause 
                Write-Host ""
                Write-Host "Please view the report at below location:" -ForegroundColor yellow 
                Write-Host "\report\docwb-dev-report\jacoco-aggregate\index.html" -ForegroundColor green 
                Write-Host ""
            }'q' {
                exit 0
            }default {
                $projectNameSelected = $menuIndexJavaProjectNameMap[$numberPart]
                if ($projectNameSelected) {
                    # TODO - Angular build
                    # if ($projectNameSelected -eq 'DocWorkbenchUIWAR') {
                    #     Set-Location "$CURRENT_DIRECTORY/$projectNameSelected/src/main/angular"
                    #     BuildAngular
                    # }
                    $envNameSelected = $menuIndexEnvNameMap[$charPart]
                    if ($envNameSelected) {
                        Set-Location $CURRENT_DIRECTORY
                        $command = "mvn -am -pl $projectNameSelected clean package -P client"
                        # $command += " ""-Dmaven.test.failure.ignore=True"""
                        $command += " ""-Dmaven.test.skip=True"""
                        if ($envNameSelected -ne "NA") {
                            $command += " ""-Ddeploy.env.name=$envNameSelected"" " 
                        }
                        # Invoke-Expression $command
                        ExecuteCommand @($command)
                    }
                }
            }
        }
    }
    until ($userinput -eq 'q')
}

### <<< Private Functions


### >>> Main Module
# -------------------- MAIN --------------------
ShowWelcomeMessage

java -version
SetWindowsTitle "$CURRENT_DIRECTORY"
LaunchRecursiveMenu

Read-Host "Press {ENTER} key to terminate..."

### <<< Main Module
