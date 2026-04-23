package com.example.yamnetwatchapp0913

data class WordItem(
    val name: String,
    var isOn: Boolean = false
)

<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.yamnetwatchapp0913">

    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.GET_ACCOUNTS" />
    <uses-permission android:name="android.permission.USE_CREDENTIALS" />
    <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

    <uses-permission android:name="android.permission.VIBRATE" />

    <queries>
        <intent>
            <action android:name="android.speech.RecognitionService" />
        </intent>
    </queries>

    <application
        android:allowBackup="true"
        android:icon="@mipmap/appicon1"
        android:label="@string/app_name"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar">

        <!-- SplashActivity를 첫 시작 액티비티로 설정 -->
        <activity
            android:name=".SplashActivity"
            android:exported="true"
            >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- 온보딩 액티비티들 -->
        <activity
            android:name=".IntroActivity"
            android:exported="false" />

        <activity
            android:name=".Intro2Activity"
            android:exported="false" />

        <activity
            android:name=".Intro3Activity"
            android:exported="false" />

        <activity
            android:name=".Intro4Activity"
            android:exported="false" />

        <activity
            android:name=".Splash2Activity"
            android:exported="false" />

        <!-- 메인 액티비티 -->
        <activity
            android:name=".MainActivity"
            android:exported="false" />

        <activity
            android:name=".ChatActivity"
            android:parentActivityName=".MainActivity"
            android:exported="false" />

        <activity
            android:name=".ConversationListActivity"
            android:parentActivityName=".ChatActivity"
            android:exported="false" />

        <activity
            android:name=".ConversationDetailActivity"
            android:parentActivityName=".ConversationListActivity"
            android:exported="false" />

        <activity
            android:name=".HochulActivity"
            android:exported="false" />

        <activity
            android:name=".SettingActivity"
            android:exported="false" />

        <activity
            android:name=".WordInsikActivity"
            android:exported="false" />

        <activity
            android:name=".SoundSettingActivity"
            android:exported="false" />

        <activity
            android:name=".SoundSetting2Activity"
            android:exported="false" />

        <activity
            android:name=".SoundSetting3Activity"
            android:exported="false" />

        <activity
            android:name=".SoundSetting4Activity"
            android:exported="false" />

        <activity
            android:name=".SoundSetting5Activity"
            android:exported="false" />

        <activity
            android:name=".SoundSetting6Activity"
            android:exported="false" />


    </application>



</manifest>


이 코드들 분석좀(안드로이드 스튜디오 main에서 java에 해당하는 전체 코드들임)
