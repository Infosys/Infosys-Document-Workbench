#!/bin/bash
# ===============================================================================================================*
# Copyright 2022 Infosys Ltd.                                                                                    *
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  *
# http://www.apache.org/licenses/                                                                                *
# ===============================================================================================================*
# Script Details: Generates certificate for VM

function echo_bright_green {
    echo -e "\033[92m$1\033[0m"
}
function echo_bright_yellow {
    echo -e "\033[93m$1\033[0m"
}
function echo_cyan {
    echo -e "\033[36m$1\033[0m"
}
function echo_dark_gray {
    echo -e "\033[90m$1\033[0m"
}

echo_bright_green "\nThis script is used for certificate generation."
echo -e ""

# CONFIGURATION BEGIN
workingdir="/tmp/certificates"
keystorepath="$workingdir/docwb.keystore"
keystorepwd="docwb12345"
keypwd="docwb12345"
# CONFIGURATION END

vmhostname=`hostname`
certificatefilepath="$workingdir/$vmhostname.cer"
keyfilepath="$workingdir/$vmhostname.key"
csrfilepath="$workingdir/$vmhostname.csr"


# Ask user if okay to proceed
echo -e "Current folder (working directory): $workingdir"
echo -e "Certificates will be generated in folder: $workingdir"
echo -e ""
read -p "Do you want to proceed? (y/n): " -r
# check what user answered
if [[ $REPLY =~ ^[Yy]$ ]]
then
    echo_dark_gray "Proceeding with certificate generation\n"
else
    echo_dark_gray "Exiting\n"
    exit 1
fi

mkdir -p $workingdir

echo_cyan "Step 1: Generate new key pair and store it in new keystore"
echo_cyan "=========================================================="
echo -e ""

keytool -genkeypair \
-keystore $keystorepath \
-alias $vmhostname \
-dname "CN=$vmhostname,OU=NA,O=NA,L=NA,S=NA,C=NA" \
-keyalg RSA \
-keysize 2048 \
-storetype PKCS12 \
-storepass $keystorepwd \
-keypass $keypwd \
-ext SAN=dns:$vmhostname

echo -e ""
echo_bright_green "Step 1: Verification"
echo_bright_green "--------------------"

keytool -list \
-keystore $keystorepath \
-storepass $keystorepwd -v | grep contains

echo_bright_yellow "Please verify from above output that keystore contains 1 entry"
echo -e ""

echo -e ""
echo_cyan "Step 2: Generate certificate file"
echo_cyan "=========================================================="
echo -e ""

openssl pkcs12 \
-in $keystorepath \
-passin pass:$keystorepwd \
-nokeys \
-out $certificatefilepath

echo -e ""
echo_cyan "Step 3: Generate key file"
echo_cyan "=========================================================="
echo -e ""

openssl pkcs12 \
-in $keystorepath \
-passin pass:$keystorepwd \
-nodes \
-nocerts \
-out $keyfilepath

echo -e ""
echo_cyan "Step 4: Generate CSR file"
echo_cyan "=========================================================="
echo -e ""

keytool -certreq \
-keystore $keystorepath \
-alias $vmhostname \
-keyalg RSA \
-sigalg SHA256withRSA \
-file $csrfilepath \
-storepass $keystorepwd \
-ext SAN=dns:$vmhostname

echo_bright_green "Step 4: Verification"
echo_bright_green "--------------------"

openssl req -noout -text -in $csrfilepath | grep DNS

echo_bright_yellow "Please verify from above output that DNS is same as VM hostname ($vmhostname)"
echo -e ""

echo -e ""
echo_cyan "Step 5: Verification"
echo_cyan "=========================================================="
echo -e ""

ls -l $workingdir/*.cer $workingdir/*.key $workingdir/*.csr $workingdir/*.keystore

echo_bright_yellow "Please verify from above output that 4 files are generated in the working directory"
echo -e ""

echo_bright_yellow "Please MOVE the 4 files generated to final destination. E.g./home/projadmin/programfiles/certificates"
echo -e ""
echo -e ""