# URL Receiver App

## Overview

This Android app continuously listens for incoming messages from a sender app over TCP/IP on port 11007. Every 30 seconds, it receives a new image URL and displays the corresponding image in a fullscreen slideshow.

## Features

- Listens for TCP messages on a specified port
- Fetches new image URLs every 30 seconds
- Displays images as a slideshow with automatic updates
- Handles network interruptions gracefully

## Setup

1. Clone the repository and open it in Android Studio.
2. Ensure the listening port is set to 11007.
3. Build and run the app on a device or emulator with internet access.
4. Make sure the sender app is running and sending data on the same port.
