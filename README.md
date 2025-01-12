# Forum Application
The Forum Application is a full-stack project built using Android, Flask, and MySQL. It provides features for user authentication, post creation, commenting, and searching through posts. This application aims to offer a Reddit-like experience with a clean interface and easy navigation.

## Architecture
The application follows an MVC-inspired architecture, with distinct layers for backend services, frontend logic, and database management.

## Backend (Flask)
The Flask backend is responsible for managing data and providing RESTful API endpoints. Key features include:

* User authentication using JWT.
* CRUD operations for posts, comments, and topics.
* Role-based operations (e.g., only post owners can edit/delete posts).

  
## Frontend (Android)
The frontend is developed using Android Studio with the following:

* Java for the core logic.
* RecyclerView for dynamic post and comment rendering.
* Volley for API communication.
* Clean, responsive UI with a focus on user experience.


## Database (MySQL)
The MySQL database is designed with clear relational structures:

* Users: For user credentials and authentication.
* Posts: For managing forum posts.
* Comments: For adding comments to posts.
* Topics: To categorize posts.

  
## Implementation Details
* Core Features
* User Registration and Login: Users can register and log in with secure credentials.

### Post Management:
* Create, edit, delete, and view posts.
* Assign posts to specific topics.
  
### Comments:
* Add comments to posts.
* Edit or delete comments by the comment owner.

### Search:
* Search posts by title or content.
* Dynamically update the UI to show filtered results.

### Authorization:
* JWT-secured endpoints ensure that only authenticated users can access and modify data.

  
## Bonus Features
### Input Validation:
* Passwords are hidden using the textPassword input type.
* Validation for unique usernames during registration.

### Error Handling:
* Comprehensive error messages for failed API requests.
* Validation messages for empty fields or invalid inputs.



## How to Run
To run this project, follow these steps:

### Backend Setup

1. Prepare Your Environment:

* Ensure Python 3.x is installed on your system.
* Install MySQL or connect to an existing MySQL database.
  
2. Clone the Repository:
```
git clone <repository-url>
cd <repository-folder>
```
3. Set Up the Database:

* Locate the mbw file in the database folder.
* Import the file into MySQL using the following command:
```
mysql -u <your-username> -p <database-name> < path/to/database/yourfile.mbw
```
* Update database credentials in the Python script if necessary.
  
4. Run the Backend Server:

* Navigate to the backend folder:
```
cd backend
```
*Run the Python script:

```
python main.py
```

5. Verify the Backend:

* Check that the Flask server is running on http://127.0.0.1:5000 or your configured IP and port.

  
### Frontend Setup (Android App)

1. Open the Project:

* Open the project in Android Studio.
  
2.Update the Backend URL:

* In the MainActivity file, update the MainActivity.URL constant to point to your backend server's IP and port:
```
public final static String URL = "http://<your-backend-ip>:5000";
```

3. Build the Project:

* Sync Gradle files in Android Studio.
* Resolve any dependency issues if prompted.
  
4. Run the Application:

* Connect a physical device or start an emulator.
* Click the "Run" button in Android Studio to deploy the app.

## API Endpoints

### User Endpoints

- **POST /api/user/register**: Register a new user.
- **POST /api/auth/login**: Authenticate a user and return a JWT token.

### Post Endpoints

- **GET /api/pos**t: Retrieve all posts.
- **POST /api/post**: Create a new post.
- **PUT /api/post/{id}**: Update an existing post (owner-only).
- **DELETE /api/post/{id**}: Delete a post (owner-only).

### Comment Endpoints

- **GET /api/comment/post/{postId}**: Retrieve all comments for a specific post.
- **POST /api/comment/{postId}**: Add a comment to a post.
- **PUT /api/comment/{id}**: Update a comment (owner-only).
- **DELETE /api/comment/{id}**: Delete a comment (owner-only).

### Topic Endpoints

- **GET /api/topic**: Retrieve all topics.
- **POST /api/post_has_topic/{postId}/{topicId}**: Assign a topic to a post.

### Technologies Used

- **Backend**: Flask, MySQL, JWT
- **Frontend**: Android Studio, Java, Volley
- **Database**: MySQL
- **UI**: Material Design, RecyclerView, Fragments

Enjoy using the Forum Application!
