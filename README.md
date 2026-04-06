# Introduction
SaveNotes is an Android application designed to provide users with an efficient way to create, manage, and revisit notes enriched with media content. Unlike traditional note-taking apps, SaveNotes integrates multiple Android components, including local database storage, background processing, hardware sensors, and notifications, to enhance user interaction and usability.

Users can capture or select images, associate them with notes, and store all data persistently. The app also incorporates intelligent reminders and motion-based interactions, making it both functional and interactive. This project demonstrates practical implementation of core Android concepts such as data persistence, UI/UX design, asynchronous processing, and sensor integration.

# System Architecture Overview
SaveNotes follows a modular architecture where each component is responsible for specific functionality:
* **User Interface Layer:** Manages input and display using Material Design components.
* **Data Layer:** Uses SQLite for local storage of notes and associated media.
* **Background Operations:** Implemented via WorkManager, allowing tasks to run reliably even when the app is inactive.
* **Sensor Layer:** Uses the accelerometer to detect motion-based triggers.
* **Notification System:** Provides timely reminders to engage users.

This layered approach ensures maintainability, scalability, and a clear separation of concerns.

# Database Structure
The app uses SQLite through the `SQLiteOpenHelper` class for efficient database management.

* **Database Name:** NotesDB_48
* **Table Name:** notes_48

| Field Name | Description |
| :--- | :--- |
| **id** | Primary key, auto-incremented |
| **title** | Stores the note title |
| **description** | Stores detailed note content |
| **image_path** | Stores the path or URI of the attached image |
| **date** | Timestamp of note creation |
| **note_type** | Extra field based on roll number (48 % 4 = 0 → note_type) |


# User Interface Design
The UI is built using Material Design 3 principles to provide a clean, modern, and intuitive experience.

**Main Screen Features:**
* Input fields for note title, description, and note type.
* Buttons for Capture Image / Select Image, Save Note, and View Notes.
* Empty state visual handling and dynamic thumbnails.

**Notes Display:**
* Notes are presented using a RecyclerView with card-based layouts.
* Each card displays: title, description, image, date, and category.
* The layout is responsive, handles device notches, and supports edge-to-edge display.

**Key UI considerations:**
* Consistent spacing and alignment.
* Rounded and elevated components for clarity.
* Empty state handling to enhance user feedback.

*Figure 1: Add Note Interface with Title, Description, Note type input, Capture image and gallery Buttons*

# Media Handling
SaveNotes supports both camera capture and gallery selection:
* **Camera:** Uses intents and FileProvider to securely save images.
* **Gallery:** Uses implicit intents to select images from device storage.
* Images are previewed before saving, and their paths are stored in the database.
* Application intent flows ensure secure permissions without crashing on modern OS versions.

*Figure 2: Note with Attached Image Demonstrating Camera/Gallery Integration and Preview Functionality*

# Recycler View
The application utilizes a RecyclerView to efficiently display the list of saved notes in a structured and scrollable format. Each note is presented using a card-based layout that includes the title, description, associated image, date, and category tag. The RecyclerView improves performance by reusing view components, making it suitable for handling multiple entries smoothly. This implementation ensures a clean visual hierarchy and enhances user experience by organizing notes in an easily readable and interactive format.

*Figure 3: Notes Display Screen Showing Multiple Entries Using RecyclerView with Card-Based Layout*  
*Figure 4: RecyclerView Demonstrating Scrollable List of Notes with Images, Categories, and Timestamps*  
*Figure 5: SQLite Database (NotesDB_48) Showing Stored Notes with Fields Including Title, Description, Image Path, Date, and note_type*

# Background Task Implementation
Background processing is implemented using WorkManager, which is the recommended Android API for deferrable and asynchronous tasks.  
A periodic task is scheduled to run at fixed intervals, ensuring that the application can perform operations even when not actively in use. This approach guarantees reliability across different device states, including app termination or system constraints.  
The background worker is responsible for triggering periodic reminders, ensuring that users remain engaged with their stored notes.

*Figure 6: WorkManager Execution Showing Periodic Background Task for Notification Scheduling*

# Notification System
The application includes both immediate and periodic notification mechanisms.  
An instant notification is triggered when the application is launched, providing immediate user engagement. In addition, periodic notifications are generated using WorkManager.  

The notification message is determined based on the roll number logic. For roll number 48, the message displayed is:  
**“Review your saved notes today”**  

A notification channel is configured to ensure compatibility with modern Android versions. Notifications are delivered using the NotificationManager service, ensuring proper system-level handling.

*Figure 7: Notification Triggered Displaying “Review your saved notes today” Message*

# Sensor Integration (Accelerometer)
The application utilizes the accelerometer sensor to detect motion-based interactions.  
Using SensorManager, acceleration values across different axes are monitored continuously. A shake detection algorithm is implemented to identify significant motion changes.  
Upon detecting a shake event, the application refreshes the note list dynamically and displays a toast message indicating device motion. This feature demonstrates how hardware sensors can be effectively used to extend application functionality beyond traditional input methods.

*Figure 8: Device Motion Detection Using Accelerometer Sensor Triggering List Refresh and Toast Message*
