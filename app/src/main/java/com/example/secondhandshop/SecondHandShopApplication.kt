package com.example.secondhandshop

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.config.CheckoutConfig
import com.paypal.checkout.config.Environment
import com.paypal.checkout.config.SettingsConfig
import com.paypal.checkout.createorder.CurrencyCode
import com.paypal.checkout.createorder.UserAction
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SecondHandShopApplication:Application() {

    val clientID = "Ach-Cs6rUK2ct0we3DF2wpMwpdysishFPwzHQSda7I7yNebztM3-QbrV9kgQIieWvC8QRGsKQ94BPk_T"
    val returnUrl = "com.example.secondhandshop://paypalpay"

    override fun onCreate() {
        super.onCreate()

        val config = CheckoutConfig(
            application = this,
            clientId = clientID,
            environment = Environment.SANDBOX,
            returnUrl = returnUrl,
            currencyCode = CurrencyCode.USD,
            userAction = UserAction.PAY_NOW,
            settingsConfig = SettingsConfig(
                loggingEnabled = true,
                showWebCheckout = false
            )
        )
        PayPalCheckout.setConfig(config)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}