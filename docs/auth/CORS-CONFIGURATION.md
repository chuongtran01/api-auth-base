# CORS Configuration Guide

## 🎯 **Overview**

CORS (Cross-Origin Resource Sharing) is configured to allow frontend applications to make requests to the authentication API from different origins. The configuration is environment-specific and customizable.

## 🔧 **Configuration Structure**

### **CorsProperties Class**

- **Location**: `src/main/java/com/authbase/config/CorsProperties.java`
- **Purpose**: Configuration properties for CORS settings
- **Prefix**: `cors`

### **CorsConfig Class**

- **Location**: `src/main/java/com/authbase/config/CorsConfig.java`
- **Purpose**: Spring configuration bean for CORS
- **Usage**: Automatically applied to all endpoints

## 🌍 **Environment-Specific Settings**

### **Development Environment (`application-dev.yml`)**

```yaml
cors:
  allowed-origin-patterns:
    - "http://localhost:3000" # React default
    - "http://localhost:4200" # Angular default
    - "http://localhost:8080" # Vue default
    - "http://localhost:5173" # Vite default
    - "*" # Allow all for development
  allowed-methods:
    - "GET"
    - "POST"
    - "PUT"
    - "DELETE"
    - "OPTIONS"
    - "PATCH"
  allow-credentials: true
  max-age: 3600
```

**Features**:

- ✅ Allows all common frontend development ports
- ✅ Permits all HTTP methods including PATCH
- ✅ Allows credentials for authentication
- ✅ 1-hour preflight cache

### **Production Environment (`application-prod.yml`)**

```yaml
cors:
  allowed-origin-patterns:
    - "https://*.yourdomain.com" # Replace with your domain
    - "https://yourdomain.com"
  allowed-methods:
    - "GET"
    - "POST"
    - "PUT"
    - "DELETE"
    - "OPTIONS"
  allow-credentials: true
  max-age: 3600
```

**Features**:

- 🔒 Restrictive origin patterns for security
- 🔒 Limited HTTP methods (no PATCH)
- ✅ Allows credentials for authentication
- ✅ 1-hour preflight cache

### **Test Environment (`application-test.yml`)**

```yaml
cors:
  allowed-origin-patterns:
    - "*"
  allowed-methods:
    - "GET"
    - "POST"
    - "PUT"
    - "DELETE"
    - "OPTIONS"
  allow-credentials: true
  max-age: 3600
```

**Features**:

- ✅ Allows all origins for testing
- ✅ Standard HTTP methods
- ✅ Allows credentials
- ✅ 1-hour preflight cache

## 🛠️ **Configuration Properties**

### **Allowed Origin Patterns**

```yaml
cors:
  allowed-origin-patterns:
    - "http://localhost:3000" # Specific origin
    - "https://*.example.com" # Wildcard subdomain
    - "https://example.com" # Specific domain
    - "*" # Allow all (development only)
```

### **Allowed HTTP Methods**

```yaml
cors:
  allowed-methods:
    - "GET" # Read operations
    - "POST" # Create operations
    - "PUT" # Update operations
    - "DELETE" # Delete operations
    - "OPTIONS" # Preflight requests
    - "PATCH" # Partial updates (optional)
```

### **Allowed Headers**

```yaml
cors:
  allowed-headers:
    - "Origin" # Origin header
    - "Content-Type" # Content type
    - "Accept" # Accept header
    - "Authorization" # JWT tokens
    - "X-Requested-With" # AJAX requests
    - "Access-Control-Request-Method" # Preflight
    - "Access-Control-Request-Headers" # Preflight
```

### **Exposed Headers**

```yaml
cors:
  exposed-headers:
    - "Authorization" # JWT tokens
    - "X-Total-Count" # Pagination
    - "X-Page-Number" # Pagination
    - "X-Page-Size" # Pagination
```

### **Credentials and Cache**

```yaml
cors:
  allow-credentials: true # Allow cookies/auth headers
  max-age: 3600 # Preflight cache in seconds
```

## 🔒 **Security Considerations**

### **Development**

- ✅ Allow all origins for easy frontend development
- ✅ Include all necessary headers
- ✅ Enable credentials for authentication testing

### **Production**

- 🔒 Restrict origins to your actual domains
- 🔒 Limit HTTP methods to what you actually use
- 🔒 Only expose necessary headers
- ✅ Keep credentials enabled for authentication

### **Testing**

- ✅ Allow all origins for test flexibility
- ✅ Standard configuration for test scenarios

## 🚀 **Usage Examples**

### **Frontend Integration (React)**

```javascript
// Example API call with CORS
const response = await fetch("http://localhost:8080/api/auth/login", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  },
  credentials: "include", // Important for cookies
  body: JSON.stringify({
    email: "user@example.com",
    password: "password",
  }),
});
```

### **Frontend Integration (Angular)**

```typescript
// Example HTTP service
@Injectable()
export class AuthService {
  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>("/api/auth/login", credentials, {
      withCredentials: true, // Important for cookies
    });
  }
}
```

## 🔧 **Customization**

### **Add Custom Headers**

```yaml
cors:
  allowed-headers:
    - "Origin"
    - "Content-Type"
    - "Accept"
    - "Authorization"
    - "X-Custom-Header" # Your custom header
```

### **Add Custom Origins**

```yaml
cors:
  allowed-origin-patterns:
    - "https://app.yourdomain.com"
    - "https://admin.yourdomain.com"
    - "https://api.yourdomain.com"
```

### **Disable Credentials**

```yaml
cors:
  allow-credentials: false # Only if you don't need cookies/auth
```

## 🐛 **Troubleshooting**

### **Common CORS Errors**

1. **"No 'Access-Control-Allow-Origin' header"**

   - Check if origin is in `allowed-origin-patterns`
   - Verify CORS configuration is loaded

2. **"Method not allowed"**

   - Check if HTTP method is in `allowed-methods`
   - Verify preflight request handling

3. **"Credentials not supported"**
   - Ensure `allow-credentials: true`
   - Check if using `withCredentials` in frontend

### **Debug Commands**

```bash
# Check CORS headers in response
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     http://localhost:8080/api/auth/login

# Test with credentials
curl -H "Origin: http://localhost:3000" \
     -H "Content-Type: application/json" \
     -X POST \
     --data '{"email":"test@example.com","password":"password"}' \
     http://localhost:8080/api/auth/login
```

## 📚 **Best Practices**

1. **Environment-Specific**: Use different settings for dev/test/prod
2. **Minimal Permissions**: Only allow what you actually need
3. **Security First**: Restrict origins in production
4. **Credentials**: Enable only if you need cookies/auth headers
5. **Cache**: Set appropriate max-age for preflight requests
6. **Testing**: Test CORS with actual frontend applications
