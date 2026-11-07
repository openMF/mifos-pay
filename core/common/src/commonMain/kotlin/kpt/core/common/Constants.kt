/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common

/**
 * Created by naman on 17/6/17.
 */
object Constants {
    /** Prefix of the HTTP Basic authorization header value ("Basic "). */
    const val BASIC = "Basic "

    /** Argument/key name "client_id". */
    const val CLIENT_ID = "client_id"

    /** Argument/key name "account_id". */
    const val ACCOUNT_ID = "account_id"

    /** Argument/key name "account". */
    const val ACCOUNT = "account"

    /** Argument/key name "to_external_id". */
    const val TO_EXTERNAL_ID = "to_external_id"

    /** Indian rupee currency symbol shown next to wallet amounts. */
    const val RUPEE = "₹"

    /** Argument/key name "qr_data". */
    const val QR_DATA = "qr_data"

    /** Argument/key name "merchant_name". */
    const val MERCHANT_NAME = "merchant_name"

    /** Argument/key name "merchant_vpa". */
    const val MERCHANT_VPA = "merchant_vpa"

    /** Argument/key name "merchant_account_no". */
    const val MERCHANT_ACCOUNT_NO = "merchant_account_no"

    /** Argument/key name "file". */
    const val FILE = "file"

    /** MIME type used for multipart form uploads (KYC documents). */
    const val MULTIPART_FORM_DATA = "multipart/form-data"

    /** User-facing text: "Choose a file to upload.". */
    const val CHOOSE_A_FILE_TO_UPLOAD = "Choose a file to upload."

    /** User-facing text: "Error uploading docs.". */
    const val ERROR_UPLOADING_DOCS = "Error uploading docs."

    /** User-facing text: "KYC Level 2 documents added successfully.". */
    const val KYC_LEVEL_2_DOCUMENTS_ADDED_SUCCESSFULLY = "KYC Level 2 documents added successfully."

    /** MIME filter matching any image type in the file picker. */
    const val IMAGE = "image/*"

    /** MIME type for PDF documents. */
    const val APPLICATION_PDF = "application/pdf"

    /** User-facing text: "Error adding KYC Level 1 details.". */
    const val ERROR_ADDING_KYC_LEVEL_1_DETAILS = "Error adding KYC Level 1 details."

    /** User-facing text: "KYC Level 1 details added successfully.". */
    const val KYC_LEVEL_1_DETAILS_ADDED_SUCCESSFULLY = "KYC Level 1 details added successfully."

    /** User-facing text: "Please wait..". */
    const val PLEASE_WAIT = "Please wait.."

    /** User-facing text: "Complete KYC". */
    const val COMPLETE_KYC = "Complete KYC"

    /** User-facing text: "Need external storage permission to browse documents.". */
    const val NEED_EXTERNAL_STORAGE_PERMISSION_TO_BROWSE_DOCUMENTS =
        "Need external storage permission to browse documents."

    /** User-facing text: "Choose File". */
    const val CHOOSE_FILE = "Choose File"

    /** User-facing text: "KYC Registration Level 2". */
    const val KYC_REGISTRATION_LEVEL_2 = "KYC Registration Level 2"

    /** User-facing text: "KYC Registration Level 1". */
    const val KYC_REGISTRATION_LEVEL_1 = "KYC Registration Level 1"

    /** Date pattern (dd/MM/yy) used when displaying short dates. */
    const val DD_MM_YY = "dd/MM/yy"

    /** User-facing text: "Error deleting card.". */
    const val ERROR_DELETING_CARD = "Error deleting card."

    /** User-facing text: "Card deleted successfully.". */
    const val CARD_DELETED_SUCCESSFULLY = "Card deleted successfully."

    /** User-facing text: "Deleting Card..". */
    const val DELETING_CARD = "Deleting Card.."

    /** User-facing text: "Error updating card.". */
    const val ERROR_UPDATING_CARD = "Error updating card."

    /** User-facing text: "Card updated successfully.". */
    const val CARD_UPDATED_SUCCESSFULLY = "Card updated successfully."

    /** User-facing text: "Invalid Credit Card Number". */
    const val INVALID_CREDIT_CARD_NUMBER = "Invalid Credit Card Number"

    /** User-facing text: "Updating Card..". */
    const val UPDATING_CARD = "Updating Card.."

    /** User-facing text: "Error adding card.". */
    const val ERROR_ADDING_CARD = "Error adding card."

    /** User-facing text: "Card added successfully.". */
    const val CARD_ADDED_SUCCESSFULLY = "Card added successfully."

    /** User-facing text: "Adding Card..". */
    const val ADDING_CARD = "Adding Card.."

    /** User-facing text: "Add Card Dialog". */
    const val ADD_CARD_DIALOG = "Add Card Dialog"

    /** User-facing text: "Edit Card Dialog". */
    const val EDIT_CARD_DIALOG = "Edit Card Dialog"

    /** User-facing text: "Saved Cards". */
    const val SAVED_CARDS = "Saved Cards"

    /** User-facing text: "Update". */
    const val UPDATE = "Update"

    /** User-facing text: "KYC Registration Level 3". */
    const val KYC_REGISTRATION_LEVEL_3 = "KYC Registration Level 3"

    /** User-facing text: "OK". */
    const val OK = "OK"

    /** User-facing text: "Scan code". */
    const val SCAN_CODE = "Scan code"

    /** User-facing text: "QR code". */
    const val QR_CODE = "QR code"

    /** User-facing text: "Failed to write data to qr". */
    const val FAILED_TO_WRITE_DATA_TO_QR = "Failed to write data to qr"

    /** User-facing text: "Error occurred". */
    const val ERROR_OCCURRED = "Error occurred"

    /** User-facing text: "Logging in..". */
    const val LOGGING_IN = "Logging in.."

    /** User-facing text: "Home". */
    const val HOME = "Home"

    /** User-facing text: "Profile". */
    const val PROFILE = "Profile"

    /** User-facing text: "Error fetching balance". */
    const val ERROR_FETCHING_BALANCE = "Error fetching balance"

    /** User-facing text: "Unable to process transfer". */
    const val UNABLE_TO_PROCESS_TRANSFER = "Unable to process transfer"

    /** User-facing text: "Transaction successful". */
    const val TRANSACTION_SUCCESSFUL = "Transaction successful"

    /** User-facing text: "Sending money...". */
    const val SENDING_MONEY = "Sending money..."

    /** User-facing text: "Insufficient balance". */
    const val INSUFFICIENT_BALANCE = "Insufficient balance"

    /** User-facing text: "Error finding Virtual Payment Address". */
    const val ERROR_FINDING_VPA = "Error finding Virtual Payment Address"

    /** User-facing text: "Error finding Mobile Number". */
    const val ERROR_FINDING_MOBILE_NUMBER = "Error finding Mobile Number"

    /** User-facing text: "Please enter a valid amount". */
    const val PLEASE_ENTER_VALID_AMOUNT = "Please enter a valid amount"

    /** Regex a virtual payment address (VPA, e.g. name@bank) must match to be accepted. */
    const val VPA_VALIDATION_REGEX = "^\\w.+@\\w+$"

    /** User-facing text: "Self Account transfer is not allowed". */
    const val SELF_ACCOUNT_ERROR = "Self Account transfer is not allowed"

    /** User-facing text: "Please enter a valid amount before making the transfer". */
    const val PLEASE_ENTER_AMOUNT = "Please enter a valid amount before making the transfer"

    /** User-facing text: "Need read contacts permission". */
    const val NEED_READ_CONTACTS_PERMISSION = "Need read contacts permission"

    /** User-facing text: "Need camera permission to scan qr code.". */
    const val NEED_CAMERA_PERMISSION_TO_SCAN_QR_CODE = "Need camera permission to scan qr code."

    /** User-facing text: "Error choosing contact". */
    const val ERROR_CHOOSING_CONTACT = "Error choosing contact"

    /** User-facing text: "Make Transfer Fragment". */
    const val MAKE_TRANSFER_FRAGMENT = "Make Transfer Fragment"

    /** User-facing text: "Please enter all the fields". */
    const val PLEASE_ENTER_ALL_THE_FIELDS = "Please enter all the fields"

    /** User-facing text: "Transfer". */
    const val TRANSFER = "Transfer"

    /** User-facing text: "Transaction Details". */
    const val TRANSACTION_DETAILS = "Transaction Details"

    /** User-facing text: "Account Number : ". */
    const val ACCOUNT_NUMBER = "Account Number : "

    /** Argument/key name "transaction". */
    const val TRANSACTION = "transaction"

    /** User-facing text: "Transactions History History". */
    const val TRANSACTIONS_HISTORY = "Transactions History History"

    /** User-facing text: "Specific Transactions History". */
    const val SPECIFIC_TRANSACTIONS = "Specific Transactions History"

    /** User-facing text: "No Transaction History Available". */
    const val HISTORY_NOT_AVAILABLE = "No Transaction History Available"

    /** Base URL for shareable transaction receipt links. */
    const val RECEIPT_DOMAIN = "https://receipt.mifospay.com/"

    /** User-facing text: "Other". */
    const val OTHER = "Other"

    /** User-facing text: "Credit". */
    const val CREDIT = "Credit"

    /** User-facing text: "Debit". */
    const val DEBIT = "Debit"

    /** User-facing text: "Receipt ID". */
    const val RECEIPT_ID = "Receipt ID"

    /** User-facing text: "Date". */
    const val DATE = "Date"

    /** User-facing text: "Transaction ID". */
    const val TRANSACTION_ID = "Transaction ID"

    /** Argument/key name "transactions". */
    const val TRANSACTIONS = "transactions"

    /** User-facing text: "Error fetching transactions". */
    const val ERROR_FETCHING_TRANSACTIONS = "Error fetching transactions"

    /** User-facing text: "transfer details". */
    const val TRANSFER_DETAILS = "transfer details"

    /** User-facing text: "Unique Payment Link copied to clipboard". */
    const val UNIQUE_PAYMENT_LINK_COPIED_TO_CLIPBOARD = "Unique Payment Link copied to clipboard"

    /** User-facing text: "Unique Payment Link". */
    const val UNIQUE_PAYMENT_LINK = "Unique Payment Link"

    /** User-facing text: "Status". */
    const val STATUS = "Status"

    /** User-facing text: "Unique Receipt Link copied to clipboard". */
    const val UNIQUE_RECEIPT_LINK_COPIED_TO_CLIPBOARD = "Unique Receipt Link copied to clipboard"

    /** User-facing text: "Unique Receipt Link". */
    const val UNIQUE_RECEIPT_LINK = "Unique Receipt Link"

    /** User-facing text: "Done". */
    const val DONE = "Done"

    /** User-facing text: "Pending". */
    const val PENDING = "Pending"

    /** User-facing text: "Item(s)". */
    const val ITEMS = "Item(s)"

    /** User-facing text: "Amount". */
    const val AMOUNT = "Amount"

    /** ISO 4217 currency code for Indian rupee. */
    const val INR = "INR"

    /** User-facing text: "Consumer". */
    const val CONSUMER = "Consumer"

    /** User-facing text: "Merchant". */
    const val MERCHANT = "Merchant"

    /** User-facing text: "Invoice". */
    const val INVOICE = "Invoice"

    /** User-facing text: "Need external storage permission to download receipt". */
    const val NEED_EXTERNAL_STORAGE_PERMISSION_TO_DOWNLOAD_RECEIPT =
        "Need external storage permission to download receipt"

    /** User-facing text: "Receipt Downloaded Successfully". */
    const val RECEIPT_DOWNLOADED_SUCCESSFULLY = "Receipt Downloaded Successfully"

    /** User-facing text: "Error downloading receipt". */
    const val ERROR_DOWNLOADING_RECEIPT = "Error downloading receipt"

    /** Application name used as the prefix of generated file names. */
    const val MIFOSPAY = "mifospay"

    /** User-facing text: "Receipt". */
    const val RECEIPT = "Receipt"

    /** File extension appended to downloaded receipts. */
    const val PDF = ".pdf"

    /** User-facing text: "Error fetching receipt". */
    const val ERROR_FETCHING_RECEIPT = "Error fetching receipt"

    /** Base URL for shareable invoice links. */
    const val INVOICE_DOMAIN = "https://invoice.mifospay.com/"

    /** User-facing text: "Sending OTP to your mobile number..". */
    const val SENDING_OTP_TO_YOUR_MOBILE_NUMBER = "Sending OTP to your mobile number.."

    /** User-facing text: "Mobile Number". */
    const val MOBILE_NUMBER = "Mobile Number"

    /** User-facing text: "Country". */
    const val COUNTRY = "Country"

    /** User-facing text: "Google Sign in failed.". */
    const val GOOGLE_SIGN_IN_FAILED = "Google Sign in failed."

    /** Intent extra key carrying the Google account given name during sign-up. */
    const val GOOGLE_GIVEN_NAME = "GOOGLE_GIVEN_NAME"

    /** Intent extra key carrying the Google account family name during sign-up. */
    const val GOOGLE_FAMILY_NAME = "GOOGLE_FAMILY_NAME"

    /** Intent extra key carrying the Google account email during sign-up. */
    const val GOOGLE_EMAIL = "GOOGLE_EMAIL"

    /** Intent extra key carrying the Google account display name during sign-up. */
    const val GOOGLE_DISPLAY_NAME = "GOOGLE_DISPLAY_NAME"

    /** Intent extra key carrying the Google account photo URI during sign-up. */
    const val GOOGLE_PHOTO_URI = "GOOGLE_PHOTO_URI"

    /** User-facing text: "Choose Signup Method". */
    const val CHOOSE_SIGNUP_METHOD = "Choose Signup Method"

    /** User-facing text: "Logging out...". */
    const val LOGGING_OUT = "Logging out..."

    /** User-facing text: "Need external storage permission to browse images". */
    const val NEED_EXTERNAL_STORAGE_PERMISSION_TO_BROWSE_IMAGES =
        "Need external storage permission to browse images"

    /** User-facing text: "Settings". */
    const val SETTINGS = "Settings"

    /** User-facing text: "Edit Profile". */
    const val EDIT_PROFILE = "Edit Profile"

    /** User-facing text: "Frequent Asked Questions". */
    const val FAQ = "Frequent Asked Questions"

    /** User-facing text: "Linked Bank Accounts". */
    const val LINKED_BANK_ACCOUNTS = "Linked Bank Accounts"

    /** User-facing text: "Bank Account Details". */
    const val BANK_ACCOUNT_DETAILS = "Bank Account Details"

    /** Argument key for the new linked bank account. */
    const val NEW_BANK_ACCOUNT = "newBankAccount"

    /** User-facing text: "Verifying mobile number..". */
    const val VERIFYING_MOBILE_NUMBER = "Verifying mobile number.."

    /** User-facing text: "Verifying Otp..". */
    const val VERIFYING_OTP = "Verifying Otp.."

    /** Label for the Mifos savings product id. */
    const val MIFOS_SAVINGS_PRODUCT_ID = "Mifos Savings Product Id"

    /** User-facing text: "Merchants". */
    const val MERCHANTS = "Merchants"

    /** User-facing text: "UPI PIN". */
    const val UPI_PIN = "UPI PIN"

    /** Argument key for the current step of a multi-step flow. */
    const val STEP = "step"

    /** User-facing text: "Type". */
    const val TYPE = "Type"

    /** User-facing text: "Setup". */
    const val SETUP = "Setup"

    /** User-facing text: "Bank deleted successfully". */
    const val BANK_DELETED_SUCCESSFULLY = "Bank deleted successfully"

    /** User-facing text: "Error occurred while changing UPI PIN". */
    const val ERROR_OCCURRED_WHILE_CHANGING_UPI_PIN = "Error occurred while changing UPI PIN"

    /** User-facing text: "Setup UPI". */
    const val SETUP_UPI = "Setup UPI"

    /** User-facing text: "Error while setting up UPI PIN". */
    const val ERROR_WHILE_SETTING_UP_UPI_PIN = "Error while setting up UPI PIN"

    /** User-facing text: "UPI PIN Setup Completed Successfully". */
    const val UPI_PIN_SETUP_COMPLETED_SUCCESSFULLY = "UPI PIN Setup Completed Successfully"

    /** User-facing text: "Forgot". */
    const val FORGOT = "Forgot"

    /** User-facing text: "Change". */
    const val CHANGE = "Change"

    /** Argument key for a one-time password. */
    const val OTP = "otp"

    /** User-facing text: "Setup UPI PIN". */
    const val SETUP_UPI_PIN = "Setup UPI PIN"

    /** User-facing text: "Setting up UPI PIN..". */
    const val SETTING_UP_UPI_PIN = "Setting up UPI PIN.."

    /** User-facing text: "Updated Bank Account". */
    const val UPDATED_BANK_ACCOUNT = "Updated Bank Account"

    /** Argument key for a list index. */
    const val INDEX = "Index"

    /** Argument key for an account number. */
    const val ACCOUNT_NO = "AccountNo"

    /** User-facing text: "Change UPI PIN". */
    const val CHANGE_UPI_PIN = "Change UPI PIN"

    /** User-facing text: "Forgot UPI PIN". */
    const val FORGOT_UPI_PIN = "Forgot UPI PIN"

    // broadcast receiver intent filters
    /** Broadcast intent action sent when push-notification registration completes. */
    const val REGISTRATION_COMPLETE = "registrationComplete"

    /** Broadcast intent action sent when a push notification is received. */
    const val PUSH_NOTIFICATION = "pushNotification"

    // id to handle the notification in the notification tray
    /** Notification-tray id for a standard notification. */
    const val NOTIFICATION_ID = 100

    /** Notification-tray id for a big-image notification. */
    const val NOTIFICATION_ID_BIG_IMAGE = 101

    /** User-facing text: "Fields cannot be empty". */
    const val ERROR_FIELDS_CANNOT_BE_EMPTY = "Fields cannot be empty"

    /** User-facing text: "Passwords are not the same". */
    const val ERROR_VALIDATING_PASSWORD = "Passwords are not the same"

    /** User-facing text: "New password can't be the same as old password.". */
    const val ERROR_PASSWORDS_CANT_BE_SAME = "New password can't be the same as old password."

    /** Key identifying the change-profile-image request. */
    const val CHANGE_PROFILE_IMAGE_KEY = "CHANGE_PROFILE_IMAGE_KEY"

    /** Value paired with CHANGE_PROFILE_IMAGE_KEY for the change-profile-image request. */
    const val CHANGE_PROFILE_IMAGE_VALUE = "CHANGE_PROFILE_IMAGE_VALUE"

    /** User-facing text: "Tap to Reveal". */
    const val TAP_TO_REVEAL = "Tap to Reveal"

    /** User-facing text: "Name : ". */
    const val NAME = "Name : "

    /** User-facing text: "Error fetching details". */
    const val ERROR_FETCHING_TRANSACTION_DETAILS = "Error fetching details"

    // const val WHITE_BACK_BUTTON = R.drawable.ic_arrow_back_white_24dp
    //  const val BLACK_BACK_BUTTON = R.drawable.ic_arrow_back_black_24dp
    /** User-facing text: "View". */
    const val VIEW = "View"

    /** Mode flag for passcode entry that verifies the current passcode. */
    const val CURRENT_PASSCODE = "current passcode"

    /** Mode flag for passcode entry that sets a new passcode. */
    const val UPDATE_PASSCODE = "update passcode"

    /** User-facing text: "SELECT DATE". */
    const val SELECT_DATE = "SELECT DATE"

    /** Argument key for a standing instruction id. */
    const val SI_ID = "standing_instruction_id"

    /** Error text matched against failures to detect an HTTP 401 response. */
    const val UNAUTHORIZED_ERROR = "401 Unauthorized"

    /** User-facing text: "Receipt link for the transaction ". */
    const val RECEIPT_SHARING_MESSAGE = "Receipt link for the transaction "

    /** Connector word used when composing share messages. */
    const val TO = " to "

    /** Separator used between a label and its value. */
    const val COLON = " : "

    /** Request code for the camera permission/intent. */
    const val REQUEST_CAMERA = 0

    /** Request code for the QR-scanner activity result. */
    const val SCAN_QR_REQUEST_CODE = 666
}
