var exec = require('cordova/exec');

var KFHWalletPlugin = {

    /** 🔹 Register once at app start */
    registerEventListener: function (success, error) {
        exec(success, error, 'KFHWalletPlugin', 'registerEventListener', []);
    },

    registerNotification: function (successCallback, errorCallback) {
        exec(function (res) {
            try {
                if (res && res.event) {
                    console.log("[EBC Plugin Event]", res.event, res.data);
                    successCallback(res.event, res.data);
                }
            } catch (err) {
                console.error("JS parsing error", err);
            }
        },
            function (err) {
                console.error("RegisterNotification error", err);
                if (errorCallback) errorCallback(err);
            }, 'KFHWalletPlugin', 'registerNotification', []);
    },

    enroll: function (data, successCallback, errorCallback) {
        exec(function (res) {
            try {
                if (res && res.event) {
                    console.log("[EBC Plugin Event]", res.event, res.data);
                    successCallback(res.event, res.data);
                } else {
                    successCallback("enrollResponse", res);
                }
            } catch (err) {
                console.error("JS parsing error", err);
            }
        },
            function (err) {
                console.error("Enrollment error", err);
                if (errorCallback) errorCallback(err);
            }, 'KFHWalletPlugin', 'enroll', [data]);
    },

    replenish: function (success, error) {
        exec(success, error, 'KFHWalletPlugin', 'replenish', []);
    },

    onCdCvmTypeSelected: function (option, success, error) {
        exec(success, error, 'KFHWalletPlugin', 'onCdCvmTypeSelected', [option]);
    },

    submitTokenPurpose: function (purpose, success, error) {
        exec(success, error, 'KFHWalletPlugin', 'submitTokenPurpose', [purpose]);
    },

    submitIdvOption: function (idvOptionJson, successCallback, errorCallback) {
        exec(function (res) {
            try {
                if (res && res.event) {
                    console.log("[IDV Event]", res.event, res.data);
                    successCallback(res.event, res.data);
                } else {
                    successCallback("submitIdvOptionResponse", res);
                }
            } catch (err) {
                console.error("JS parsing error", err);
            }
        },
            function (err) {
                console.error("IDV option submit error", err);
                if (errorCallback) errorCallback(err);
            }, 'KFHWalletPlugin', 'submitIdvOption', [idvOptionJson]);
    },

    submitOtp: function (otpCode, success, error) {
        exec(success, error, 'KFHWalletPlugin', 'submitOtp', [otpCode]);
    },

    acceptTnc: function (success, error) {
        exec(success, error, 'KFHWalletPlugin', 'acceptTnc', []);
    },

    getWalletCardsMaxCount: function (successCallback, error) {
        exec(function (res) {
            try {
                console.log("[EBC Plugin Event]", res.event, res.data);
                successCallback(res.event, res.data);
            } catch (err) {
                console.error("Parse error", err);
                if (error) error(err);
            }
        }, function (err) {
            console.error("Error in getWalletCardsMaxCount", err);
            if (error) error(err);
        }, 'KFHWalletPlugin', 'getWalletCardsMaxCount', []);
    },

    getCardsWithEnrollmentStatus: function (successCallback, errorCallback) {
        exec(function (res) {
            try {
                if (res && res.event) {
                    console.log("[EBC Plugin Event]", res.event, res.data);
                    successCallback(res.event, res.data);
                } else {
                    successCallback("getCardsResponse", res);
                }
            } catch (err) {
                console.error("JS parsing error", err);
            }
        },
            function (err) {
                console.error("GetCards error", err);
                if (errorCallback) errorCallback(err);
            }, 'KFHWalletPlugin', 'getCardsWithEnrollmentStatus', []);
    },

    getCardDetails: function (cardData, successCallback, errorCallback) {
        exec(function (res) {
            try {
                if (res && res.event) {
                    console.log("[EBC Plugin Event]", res.event, res.data);
                    successCallback(res.event, res.data);
                } else {
                    successCallback("getCardDetailsSuccess", res);
                }
            } catch (err) {
                console.error("JS parsing error", err);
            }
        }, function (err) {
            console.error("GetCards Details error", err);
            if (errorCallback) errorCallback(err);
        }, 'KFHWalletPlugin', 'getCardDetails', [cardData]);
    },

    suspendCard: function (cardData, successCallback, errorCallback) {
        exec(function (res) {
            try {
                if (res && res.event) {
                    console.log("[EBC Plugin Event]", res.event, res.data);
                    successCallback(res.event, res.data);
                } else {
                    successCallback("suspendCardSuccess", res);
                }
            } catch (err) {
                console.error("JS parsing error", err);
            }
        },
            function (err) {
                console.error("SuspendCard error", err);
                if (errorCallback) errorCallback(err);
            }, 'KFHWalletPlugin', 'suspendCard', [cardData]);
    },

    deleteToken: function (tokenId, success, error) {
        exec(function (res) {
            try {
                if (res && res.event) {
                    console.log("[EBC Plugin Event]", res.event, res.data);
                    if (success) success(res.event, res.data);
                } else {
                    if (success) success("deleteTokenSuccess", res);
                }
            } catch (err) {
                console.error("JS parsing error", err);
            }
        },
            function (err) {
                console.error("DeleteToken error", err);
                if (error) error(err);
            }, 'KFHWalletPlugin', 'deleteToken', [tokenId]);
    },

    deleteCard: function (cardData, successCallback, errorCallback) {
        exec(function (res) {
            try {
                if (res && res.event) {
                    console.log("[EBC Plugin Event]", res.event, res.data);
                    successCallback(res.event, res.data);
                } else {
                    successCallback("deleteCardResponse", res);
                }
            } catch (err) {
                console.error("JS parsing error", err);
            }
        },
            function (err) {
                console.error("DeleteCard error", err);
                if (errorCallback) errorCallback(err);
            }, 'KFHWalletPlugin', 'deleteCard', [cardData]);
    },

    changeTokenDefaultState: function (tokenId, isDefault, success, error) {
        exec(function (res) {
            try {
                if (res && res.event) {
                    console.log("[EBC Plugin Event]", res.event, res.data);
                    if (success) success(res.event, res.data);
                } else {
                    if (success) success("changeTokenDefaultStateSuccess", res);
                }
            } catch (err) {
                console.error("JS parsing error", err);
            }
        },
            function (err) {
                console.error("ChangeTokenDefaultState error", err);
                if (error) error(err);
            }, 'KFHWalletPlugin', 'changeTokenDefaultState', [tokenId, isDefault]);
    },

    resumeToken: function (tokenId, success, error) {
        exec(function (res) {
            try {
                if (res && res.event) {
                    console.log("[EBC Plugin Event]", res.event, res.data);
                    if (success) success(res.event, res.data);
                } else {
                    if (success) success("resumeTokenSuccess", res);
                }
            } catch (err) {
                console.error("JS parsing error", err);
            }
        },
            function (err) {
                console.error("ResumeToken error", err);
                if (error) error(err);
            }, 'KFHWalletPlugin', 'resumeToken', [tokenId]);
    },

    // ✅ Check NFC Status
    checkNfcStatus: function (success, error) {
        exec(
            function (res) {
                try {
                    if (res && res.event) {
                        console.log("[KFHWalletPlugin Event]", res.event, res.data);
                        if (typeof success === "function") success(res.event, res.data);
                    } else {
                        if (typeof success === "function") success("checkNfcStatus", res);
                    }
                } catch (err) {
                    console.error("[KFHWalletPlugin] JS parsing error", err);
                    if (typeof error === "function") error(err);
                }
            },
            function (err) {
                console.error("[KFHWalletPlugin] checkNfcStatus error", err);
                if (typeof error === "function") error(err);
            },
            'KFHWalletPlugin',
            'checkNfcStatus',
            []
        );
    },

    // ✅ Open NFC Settings Screen
    openNfcSettings: function (success, error) {
        exec(
            function (res) {
                if (res && res.event) {
                    if (typeof success === "function") success(res.event, res.data);
                } else {
                    if (typeof success === "function") success("openNfcSettings", res);
                }
            },
            error,
            'KFHWalletPlugin',
            'openNfcSettings',
            []
        );
    },

    // ✅ Ask Default Payment App
    askDefaultPaymentApp: function (success, error) {
        exec(
            function (res) {
                try {
                    if (res && res.event) {
                        console.log("[KFHWalletPlugin Event]", res.event, res.data);
                        if (typeof success === "function") success(res.event, res.data);
                    } else {
                        if (typeof success === "function") success("defaultPaymentAppSuccess", res);
                    }
                } catch (err) {
                    console.error("[KFHWalletPlugin] JS parsing error", err);
                    if (typeof error === "function") error(err);
                }
            },
            function (err) {
                console.error("[KFHWalletPlugin] askDefaultPaymentApp error", err);
                if (typeof error === "function") error(err);
            },
            'KFHWalletPlugin',
            'askDefaultPaymentApp',
            []
        );
    },

    // ✅ Is Default Payment App
    isDefaultPaymentApp: function (success, error) {
        exec(
            function (res) {
                if (res && res.event) {
                    if (typeof success === "function") success(res.event, res.data);
                } else {
                    if (typeof success === "function") success("isDefaultPaymentApp", res);
                }
            },
            error,
            'KFHWalletPlugin',
            'isDefaultPaymentApp',
            []
        );
    },

    // ✅ Observe NFC Settings Listener (ON/OFF state changes)
    observeNFCSettingsListener: function (success, error) {
        exec(
            function (res) {
                if (res && res.event) {
                    if (typeof success === "function") success(res.event, res.data);
                } else {
                    if (typeof success === "function") success("observeNfcSettingsListenerSuccess", res);
                }
            },
            error,
            'KFHWalletPlugin',
            'observeNFCSettingsListener',
            []
        );
    },

    unregisterNfcSettingsListener: function (success, error) {
        exec(function (res) {
            if (success) success(res.event, res.data);
        },
            function (err) {
                console.error("[KFHWalletPlugin] error", err);
                if (typeof error === "function") error(err);
            }
            , 'KFHWalletPlugin', 'unregisterNfcSettingsListener', []);
    },
    preparePayment: function (waTokenList, success, error) {
        exec(function (res) {
            if (success) success(res.event, res.data);
        },
            function (err) {
                console.error("[KFHWalletPlugin] error", err);
                if (typeof error === "function") error(err);
            }
            , 'KFHWalletPlugin', 'preparePayment', [waTokenList]);
    },
    proceedToTransactionConfirmation: function (tokenId, success, error) {
        exec(function (res) {
            console.log("[KFHWalletPlugin Event]", res.event, res.data);
            if (success) success(res.event, res.data);
        },
            function (err) {
                console.error("[KFHWalletPlugin] error", err);
                if (typeof error === "function") error(err);
            }
            , 'KFHWalletPlugin', 'proceedToTransactionConfirmation', [tokenId]);
    },

    processPaymentResult: function (tokenId, success, error) {
        exec(function (res) {
            console.log("[KFHWalletPlugin Event]", res.event, res.data);
            if (success) success(res.event, res.data);
        },
            function (err) {
                console.error("[KFHWalletPlugin] error", err);
                if (typeof error === "function") error(err);
            }
            , 'KFHWalletPlugin', 'processPaymentResult', [tokenId]);
    },

    startAutomaticPaymentListener: function (success, error) {
        exec(function (res) {
            if (success) success(res.event, res.data);
        },
            function (err) {
                console.error("[KFHWalletPlugin] error", err);
                if (typeof error === "function") error(err);
            }
            , 'KFHWalletPlugin', 'startAutomaticPaymentListener', []);
    },
    stopAutomaticPaymentListener: function (success, error) {
        exec(function (res) {
            if (success) success(res.event, res.data);
        },
            function (err) {
                console.error("[KFHWalletPlugin] error", err);
                if (typeof error === "function") error(err);
            }
            , 'KFHWalletPlugin', 'stopAutomaticPaymentListener', []);
    },
    // ✅ Check Biometric Availability
    checkBiometricAvailability: function (success, error) {
        exec(
            function (res) {
                try {
                    if (res && res.event) {
                        console.log("[KFHWalletPlugin Event]", res.event, res.data);
                        if (typeof success === "function") success(res.event, res.data);
                    } else {
                        if (typeof success === "function") success("checkBiometricAvailability", res);
                    }
                } catch (err) {
                    console.error("[KFHWalletPlugin] JS parsing error", err);
                    if (typeof error === "function") error(err);
                }
            },
            function (err) {
                console.error("[KFHWalletPlugin] checkBiometricAvailability error", err);
                if (typeof error === "function") error(err);
            },
            'KFHWalletPlugin',
            'checkBiometricAvailability',
            []
        );
    },
    getTransactionHistory: function (tokenId, success, error) {
        exec(function (res) {
            console.log("[KFHWalletPlugin Event]", res.event, res.data);
            if (success) success(res.event, res.data);
        },
            function (err) {
                console.error("[KFHWalletPlugin] error", err);
                if (typeof error === "function") error(err);
            }
            , 'KFHWalletPlugin', 'transactionHistory', [tokenId]);
    },
};

module.exports = KFHWalletPlugin;
// window.KFHWalletPlugin = module.exports;

// Also expose globally for OutSystems
window.KFHWalletPlugin = KFHWalletPlugin;
