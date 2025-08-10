# NeedItNow - Community Sharing and Task Management Platform

NeedItNow is a web-based platform that connects neighbors and community members to share items, request help, and collaborate on tasks. The platform aims to foster stronger community bonds while making it easier for people to get help and share resources.

## Features

### Community Management
- Create and join local communities
- Community management with admin roles
- Location-based community discovery
- Community member management

### Item Sharing
- Share items with community members
- Browse available items
- Request items from neighbors
- Item categories and filtering
- Photo upload support
- Chat system for item requests

### Task Management (TaskBuddy)
- Create and manage tasks
- Request help from neighbors
- Offer assistance to others
- Task categories:
  - Pickup & Delivery
  - Pet Care
  - Elderly Help
  - Tech Help
  - Tutoring
  - Urgent Assistance
- Task status tracking
- Task history and completion records

### Group Buying
- Organize group purchases
- Manage member participation
- Track quantities and payments
- Member scoring system
- Receipt and delivery tracking

### User Features
- User registration and authentication
- Profile management
- Contact information sharing
- Activity tracking
- Notification system

## Technical Stack

- Backend: Spring Boot
- Database: JPA/Hibernate
- Frontend: HTML templates
- Security: Spring Security
- RESTful API endpoints

## Project Structure

```
src/main/java/com/needitnow/
├── controllers/        # Web controllers
├── entity/             # Database entities
├── service/            # Business logic
├── repository/         # Database repositories
└── config/            # Configuration classes
```

## Getting Started

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   ```
2. **Open the project in your IDE** (Eclipse/IntelliJ recommended)
3. **Set up the MySQL database**
   - Open MySQL Workbench (or your preferred tool).
   - Create a new database named `needitnowv1`:
     ```sql
     CREATE DATABASE needitnowv1;
     ```
4. **Configure database credentials**
   - In `src/main/resources/application.properties`, set your MySQL username and password:
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/needitnowv1
     spring.datasource.username=YOUR_USERNAME
     spring.datasource.password=YOUR_PASSWORD
     ```
5. **Build and run the Spring Boot application**
   - You can run the main class `NeedItNowApplication.java` from your IDE, or use Maven:
     ```bash
     ./mvnw spring-boot:run
     ```
6. **Access the application**
   - Open your browser and go to: [http://localhost:8081](http://localhost:8081)
7. **Register and log in**
   - Create a user account to start sharing or requesting items and tasks.

## Database Schema

The application uses several key entities:
- User: Stores user information and authentication
- Community: Represents neighborhood communities
- Item: Represents shareable items
- Task: Represents task requests and offers
- GroupBuy: Manages group purchases
- ChatMessage: Handles messaging between users

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

[Specify license here]

## Contact

For support or inquiries, please contact [manikantagopavaram0603@gmail.com]
