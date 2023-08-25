package com.example.worktracker.presentation.views.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.worktracker.R
import com.example.worktracker.data.AppDataSource
import com.example.worktracker.ui.theme.WorkTrackerTheme
import com.example.worktracker.utils.Constants.firebaseAuth
import org.koin.android.ext.android.inject

class AuthenticationActivity : ComponentActivity() {

    private val appDataSource: AppDataSource by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth.currentUser?.let{
            iniciandoLogin()
        }
        setContent{
            LoginScreen(
                validarEditTexts = { email, password ->
                    validarEditTexts(email,password)
                }
            )
        }
    }

    private fun validarEditTexts(email: String, password: String){
        val inputMethodManager = getSystemService(
            Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(
            currentFocus?.windowToken, 0)

        if(email.isEmpty() || password.isEmpty()){
            return Toast.makeText(
                this,
                "Antes de iniciar sesión, completa ambos campos.",
                Toast.LENGTH_LONG
            ).show()
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnFailureListener{
                Toast.makeText(
                    this,
                    it.message,
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnSuccessListener{
                iniciandoLogin()
            }
    }

    private fun iniciandoLogin() {
        val intent = Intent(
            this@AuthenticationActivity,
            MainActivity::class.java
        )
        finish()
        startActivity(intent)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(validarEditTexts: (String, String) -> Unit) {

    val emailText = remember { mutableStateOf("") }
    val passwordText = remember { mutableStateOf("") }


    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        /* Title */
        Text(
            text = "Work Tracker",
            modifier = Modifier
                .padding(bottom = 16.dp)
        )

        /* Email */
        OutlinedTextField(
            value = emailText.value,
            onValueChange = { emailText.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            label = { Text(text = stringResource(R.string.email)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        /* Password */
        OutlinedTextField(
            value = passwordText.value,
            onValueChange = { passwordText.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            label = { Text(text = stringResource(R.string.password)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.outline_remove_red_eye_24),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .scale(0.8f)
                )
            }
        )

        /* Iniciar sesión */
        Button(
            onClick = { validarEditTexts(emailText.value, passwordText.value) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            content = {
                Text(
                    text = stringResource(R.string.button_login),
                    color = Color.White
                )
            }
        )
    }
}

@Preview
@Composable
fun PreviewLoginScreen() {
    WorkTrackerTheme {
        LoginScreen(
            validarEditTexts = { _, _ -> }
        )
    }
}
