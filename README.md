# Backend for OCR me Photo Scanner Image Translator Recognition
![Icon](https://s15.postimg.cc/amh7izcq3/48x48.png) OCR & Translate Android app - [link to Google Play Market](https://play.google.com/store/apps/details?id=com.ashomok.ocrme)

![Alt Text](https://s15.postimg.cc/9ix37bhvv/ezgif.com-video-to-gif.gif)

## Scan photo and get text or PDF 

Get result as Editable Shareable Translatable plain text or Searchable PDF.   
For PDF you can also download it or open in another app. 
Material design.

## Content of project
### Servlets:
*	**ListOCRRequestsServlet** - retrive data (list of OCR requests) from Google Datastore using userToken
*	**OcrForTextServletDeprecated** - (Deprecated - use OcrRequestServlet instead) retrive text from image file using language param (optional)
*	**OcrRequestServlet** - retrive OcrResponse from Google Cloud Storage Uri using language (optional) and user's token (optional)
*	**SupportedLanguagesServlet** - retrive list of languages which supported by Microsoft Bing Translation API
*	**TranslateServlet** - provide TranslateResponse for sourceText and targetLanguage using Microsoft Bing Translation API
*	**TranslateServletDeprecated** - (Deprecated because of price - use OcrRequestServlet instead) provide TranslateResponseDeprecated for sourceText and targetLanguage using Google Translation API

## Technologies:
**Note:** This is Google App Engine Backend app for Android app. This project shows code of Backend app only. 

*	Google App Engine Standard Environment
*	Google Cloud Storage, Google Cloud Database
*	Google Vision API, Microsoft Bing Translation API
*	Google Auth, Firebase

## Reusage:
Rename all files with name **secret.properties.example** with **secret.properties**. Edit it's content using your data. 

## Developed by:

Yuliia Ashomok ashomokdev@gmail.com



