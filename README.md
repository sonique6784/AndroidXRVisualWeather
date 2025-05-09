# Weather XR

Weather XR demonstrate how Android Developers can use a stack of technologies including Android built-in voice-to-text, Gemini, vertex AI and 3D models, to create immersive experiences on Android XR.

## A new technology chapter
We are living in a very exciting moment in history. The rise of Artificial Intelligence together with very powerful chips allow technology to advance and to realise the future imagined in Sci-fi books 40 years ago, think Neuromancer, Ready Player one and Snow crash.

## The best time to be a developer
"Honestly, anybody who's a computer scientist should not be retired right now," ... "very unique time in history," ... "There's just never been a greater, sort of, problem and opportunity — greater cusp of technology," Sergey Brin, May 2025.

# Mixing technology together to offer immersive experiences
This project use the following technologies:
 - Android built-in voice-to-text
 - Open-meteo API to get weather
 - Gemini API to generate JSON
 - Gemini API to generate images

## XR device, solving the input problem
XR device like VR Headset and Smart glasses offer an immersive way consume content and to interact with your app. however creating, inputing content can be more challenging. when it comes to text input the best way is to use voice-to-text so the user can interact naturally with the device. Android offer voice recognition API that is available since API Level 8 (Froyo, 2010). It got a lifting with Android 34 (Upside Down Cake, 2023).

So I use this API to allow the user to perform requests to this Weather app.

## Using Gemini to talk JSON
With this project I wanted to see how good LLM like Gemini have become. I want to see if I could use Gemini as an API to generate JSON response to my requests. 
I leveraged the Gemini Flash 2.5 model that I configured to return a specific JSON format. Gemini Flash 2.5 is fast and smart, perfect for quick API calls.
I crafted a prompt the instruct Gemini to return the city and the GPS coordinates from the user's natural language request. Gemini doesn't disappoint.

## Using Open-meteo to get realtime weather
I wanted accurate and realtime weather forecast, I picked Open Meteo API from Meteo France. I use retrofit to perform the request to the API. It uses the data from Gemini Flash 2.5 JSON response. This API returns a JSON response. nothing too exciting, let's move on.

## Offering rich experiences
Generative AI is fun to work with and I added Imagen 3 to the party. Thanks to a clever prompt engineering, I use the Open Meteo response to instruct Imagen 3 to generate a visual representation of the weather, 8-bit style.

## Going the extra mile.
XR immerse the user in a new world and i wanted to take advantage of this oportunity. i added 3d models to make the user feel the weather.




