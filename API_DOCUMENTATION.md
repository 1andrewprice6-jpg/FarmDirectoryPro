# FarmDirectoryUpgraded API Documentation

## Database Schema

### Tables
Each entity in the data layer represents a database table with the following structure:

## Data Access Layer (DAOs)

All DAOs provide the following standard operations:
- `insert()`: Add new records
- `update()`: Modify existing records
- `delete()`: Remove records
- `getAll()`: Retrieve all records
- Custom queries for specific use cases

## ViewModel APIs

### State Management
All ViewModels expose state through StateFlow:
```kotlin
val items: StateFlow<List<Entity>>
val isLoading: StateFlow<Boolean>
val error: StateFlow<String?>
```

### Common Operations
- Data CRUD operations
- Search and filtering
- Sorting
- State updates

## UI Component APIs

### Composable Functions
All screen composables follow this pattern:
```kotlin
@Composable
fun Screen(
    navController: NavController,
    viewModel: ViewModel = viewModel()
)
```

## Navigation Routes
- Main screen routes
- Detail screen routes
- Settings/configuration routes

## Permissions Required
- Location permissions (if applicable)
- Storage permissions (if applicable)
- Network permissions

## Error Handling
All operations implement proper error handling:
- Try-catch blocks for exceptions
- Error state propagation to UI
- User-friendly error messages
