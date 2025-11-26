#!/bin/bash

set -e

##### Constants
PROPERTIES_FILE=local.properties

WA_CORE_PATH=""
WA_POSTFINANCE_PATH=""
WA_DEMO_APP_PATH=""

##### Default values
ENVIRONMENT="dev"
CUSTOMER="yourbank"
SDK_VERSION="1.2.0"
TESTING_CONFIGURATION="NONE"
SECURITY_VIOLATION_ACTION="log"
CARD_DIGITIZATION_METHODS='[\"MANUAL\",\"OPAQUE\"]'
CARDHOLDER_VERIFICATION_TYPES='[\"LVP_LOCKED_HVP_UNLOCKED\",\"LVP_UNLOCKED_HVP_DOUBLE_TAP\"]'

#####

function build_core() {
    echo ""
    echo "Building WA CORE..."
    if [ ! -d $WA_CORE_PATH ]; then
        echo "WA CORE directory $WA_CORE_PATH DOES NOT exists."
    fi
    pushd $WA_CORE_PATH > /dev/null 2>&1
    ./gradlew clean build publish $USE_WMS_MOCK $SKIP_PROTECTION -Penv=$ENVIRONMENT -PsecurityViolationAction=$SECURITY_VIOLATION_ACTION -PtestingConfiguration=$TESTING_CONFIGURATION -x lint $SKIP_TESTS
    if [ $? -eq 0 ]
    then
      echo "Success."
    else
      echo "Failure." >&2
      exit 1
    fi
    popd > /dev/null 2>&1

    echo "Building of WA CORE completed."
}

function build_postfinance() {
    echo ""
    echo "Building customer POSTFINANCE..."
    if [ ! -d $WA_POSTFINANCE_PATH ]; then
        echo "POSTFINANCE directory $WA_POSTFINANCE_PATH DOES NOT exists."
    fi
    pushd $WA_POSTFINANCE_PATH > /dev/null 2>&1
    ./gradlew clean build publish $USE_WMS_MOCK $SKIP_PROTECTION -Penv=$ENVIRONMENT -PsecurityViolationAction=$SECURITY_VIOLATION_ACTION -x lint $SKIP_TESTS
    if [ $? -eq 0 ]
    then
      echo "Success."
    else
      echo "Failure." >&2
      exit 1
    fi
    popd > /dev/null 2>&1

    echo "Building of customer POSTFINANCE completed."
}

function build_demo_app() {
    echo ""
    echo "Building WA DEMO APP..."
    if [ ! -d $WA_DEMO_APP_PATH ]; then
        echo "WA DEMO APP directory $WA_DEMO_APP_PATH DOES NOT exists."
    fi
    pushd $WA_DEMO_APP_PATH > /dev/null 2>&1

    if [ "$TESTING_CONFIGURATION" = "REGTOOL" ] || [ "$TESTING_CONFIGURATION" = "regtool" ] ; then
        ./gradlew clean installMinApi21PersonalizationRegtoolDebug -Penv=$ENVIRONMENT -PcustomerName=$CUSTOMER -PsdkVersion=$SDK_VERSION -PcardDigitizationMethods=$CARD_DIGITIZATION_METHODS -PcardholderVerificationTypes=$CARDHOLDER_VERIFICATION_TYPES $IS_SNAPSHOT -x lint $SKIP_TESTS
    else
        ./gradlew clean installMinApi21PersonalizationNoneDebug -Penv=$ENVIRONMENT -PcustomerName=$CUSTOMER -PsdkVersion=$SDK_VERSION -PcardDigitizationMethods=$CARD_DIGITIZATION_METHODS -PcardholderVerificationTypes=$CARDHOLDER_VERIFICATION_TYPES $IS_SNAPSHOT -x lint $SKIP_TESTS
    fi
    if [ $? -eq 0 ]
    then
      echo "Success."
    else
      echo "Failure." >&2
      exit 1
    fi
    popd > /dev/null 2>&1

    echo "Building of WA DEMO APP completed."
}

function start_demo_app() {
    echo ""
    echo "Starting WA DEMO APP..."
    adb shell am start -n com.idemia.wa.app/.MainActivity
}

function usage() {
cat <<EOF
Usage:
        $0 [options]
option:
        -a, --securityViolationAction=<SECURITY VIOLATION ACTION> - security violation action: empty, log, exception, termination; default: log
        -e, --environment=<ENVIRONMENT NAME>                      - environment, default: dev
        -c, --customer=<CUSTOMER NAME>                            - customer name, default: yourbank
        -v, --sdkVersion=<SDK VERSION>                            - customer sdk version, default: 1.2.0
        -m, --useWmsMock                                          - use WMS mock instead of real backend
        -p, --skipProtection                                      - skip GuardIT obfuscation
        -s, --snapshot                                            - sets customer sdk version to snapshot, default: true
        -t, --skipTests                                           - skip tests
        -u, --testingConfiguration=<TESTING CONFIG>               - UL_VISA, UL_MASTERCARD, REGTOOL, etc. default: NONE
        --cardDigitizationMethods=<DIGITIZATION METHODS JSON>     - MANUAL, OPAQUE, default: ["MANUAL", "OPAQUE"]; make sure to escape the quote mark for further JSON processing, see default.properties for reference
        --cardholderVerificationTypes=<VERIFICATION TYPES JSON>   - LVP_LOCKED_HVP_UNLOCKED, LVP_UNLOCKED_HVP_DOUBLE_TAP, default: ["LVP_LOCKED_HVP_UNLOCKED", "LVP_UNLOCKED_HVP_DOUBLE_TAP"]; make sure to escape the quote mark for further JSON processing, see default.properties for reference

Important: Set source code paths inside script!

EOF
    exit 1
}

function show_properties_content() {
cat <<EOF
Sample content:

WA_CORE_PATH=/path/to/wallet-agent-core
WA_POSTFINANCE_PATH=/path/to/wallet-agent-postfinance
WA_DEMO_APP_PATH=/path/to/wallet-agent-demo-app

EOF
}

#####
IS_SNAPSHOT=""
SKIP_PROTECTION=""
USE_WMS_MOCK=""
SKIP_TESTS=""

for i in "$@"
do
case $i in
    -a=*|--securityViolationAction=*)
    SECURITY_VIOLATION_ACTION="${i#*=}"
    shift
    ;;
    -e=*|--environment=*)
    ENVIRONMENT="${i#*=}"
    shift
    ;;
    -c=*|--customer=*)
    CUSTOMER="${i#*=}"
    shift
    ;;
    -v=*|--sdkVersion=*)
    SDK_VERSION="${i#*=}"
    shift
    ;;
    -u=*|--testingConfiguration=*)
    TESTING_CONFIGURATION="${i#*=}"
    shift
    ;;
    --cardDigitizationMethods=*)
    CARD_DIGITIZATION_METHODS="${i#*=}"
    shift
    ;;
    --cardholderVerificationTypes=*)
    CARDHOLDER_VERIFICATION_TYPES="${i#*=}"
    shift
    ;;
    -s|--snapshot|-s=*|--snapshot=*)
    IS_SNAPSHOT="-PisSnapshot"
    shift
    ;;
    -p|--skipProtection)
    SKIP_PROTECTION=" -PskipProtection"
    shift
    ;;
    -m|--useWmsMock)
    USE_WMS_MOCK=" -PuseWmsMock"
    shift
    ;;
    -t|--skipTests)
    SKIP_TESTS=" -x test"
    shift
    ;;
    -h|--help)
    usage
    shift
    ;;
    --default)
    DEFAULT=""
    shift
    ;;
    *)

    ;;
esac
done

if [ ! -f "$PROPERTIES_FILE" ]; then
    echo "Missing $PROPERTIES_FILE file."
    show_properties_content
    exit 1
fi

source $PROPERTIES_FILE

if [ "" = "$WA_CORE_PATH" ] || [ "" = "$WA_POSTFINANCE_PATH" ] || [ "" = "$WA_DEMO_APP_PATH" ]; then
    echo "Set source code paths in the $PROPERTIES_FILE file (same location as this script)."
    show_properties_content
    exit
fi

echo "Build parameters:"
echo "ENVIRONMENT                   = ${ENVIRONMENT}"
echo "CUSTOMER                      = ${CUSTOMER}"
echo "CUSTOMER SDK_VERSION          = ${SDK_VERSION}"
echo "SECURITY VIOLATION ACTION     = ${SECURITY_VIOLATION_ACTION}"
echo "TESTING CONFIGURATION         = ${TESTING_CONFIGURATION}"
echo "SKIP PROTECTION               = ${SKIP_PROTECTION}"
echo "USE WMS MOCK                  = ${USE_WMS_MOCK}"
echo "CARD DIGITIZATION METHODS     = ${CARD_DIGITIZATION_METHODS}"
echo "CARDHOLDER VERIFICATION TYPES = ${CARDHOLDER_VERIFICATION_TYPES}"
if [ "" = "$IS_SNAPSHOT" ]; then
    echo "SNAPSHOT VERSION              = false"
else
    echo "SNAPSHOT VERSION              = true"
fi

build_core
if [ "postfinance" = "$CUSTOMER" ]; then
    build_postfinance
fi
build_demo_app
start_demo_app

echo ""
echo "APK location: $WA_DEMO_APP_PATH/app/build/outputs/apk/"

