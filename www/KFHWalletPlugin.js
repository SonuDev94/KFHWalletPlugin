var exec = require('cordova/exec');

var KFHWalletPlugin = {

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

};

module.exports = KFHWalletPlugin;
// window.KFHWalletPlugin = module.exports;

// Also expose globally for OutSystems
window.KFHWalletPlugin = KFHWalletPlugin;