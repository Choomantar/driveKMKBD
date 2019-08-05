package com.raywenderlich.googledrivedemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

class TestSheetsApiActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    companion object {
        private const val REQUEST_SIGN_IN = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestSignIn(this)
    }

    override fun onDestroy() {
        super.onDestroy()
       // cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                GoogleSignIn.getSignedInAccountFromIntent(data)
                        .addOnSuccessListener { account ->
                            val scopes = listOf(SheetsScopes.SPREADSHEETS)
                            val credential = GoogleAccountCredential.usingOAuth2(this, scopes)
                            credential.selectedAccount = account.account

                            val jsonFactory = JacksonFactory.getDefaultInstance()
                            // GoogleNetHttpTransport.newTrustedTransport()
                            val httpTransport =  AndroidHttp.newCompatibleTransport()
                            val service = Sheets.Builder(httpTransport, jsonFactory, credential)
                                    .setApplicationName(getString(R.string.app_name))
                                    .build()

                            createSpreadsheet(service)
                        }
                        .addOnFailureListener { e ->
                            Timber.e(e)
                        }
            }
        }
    }

    private fun requestSignIn(context: Context) {
        /*
        GoogleSignIn.getLastSignedInAccount(context)?.also { account ->
            Timber.d("account=${account.displayName}")
        }
         */

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // .requestEmail()
                // .requestScopes(Scope(SheetsScopes.SPREADSHEETS_READONLY))
                .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
                .build()
        val client = GoogleSignIn.getClient(context, signInOptions)

        startActivityForResult(client.signInIntent, REQUEST_SIGN_IN)
    }

    private fun createSpreadsheet(service: Sheets) {
        var spreadsheet = Spreadsheet()
                .setProperties(
                        SpreadsheetProperties()
                                .setTitle("CreateNewSpreadsheet")
                )

        launch(Dispatchers.Default) {
            spreadsheet = service.spreadsheets().create(spreadsheet).execute()
            Timber.d("ID: ${spreadsheet.spreadsheetId}")
        }
    }
}
