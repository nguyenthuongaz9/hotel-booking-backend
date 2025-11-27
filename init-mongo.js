// File: init-mongo.js
// Khởi tạo MongoDB cho Hotel Booking System - Phiên bản phù hợp với User model

// Chuyển sang database hotelbooking
db = db.getSiblingDB('hotelbooking');

print('=== BẮT ĐẦU KHỞI TẠO MONGODB CHO HOTEL BOOKING ===');

// ==================== TẠO COLLECTIONS ====================
print('1. Đang tạo collections...');

db.createCollection('user'); // Collection name phải khớp với @Document(collection="user")
db.createCollection('refresh_tokens');

print('✓ Đã tạo xong collections');

// ==================== TẠO INDEXES ====================
print('2. Đang tạo indexes...');

// Indexes cho user collection - theo đúng model
db.user.createIndex({ "email": 1 }, { 
    name: "email_unique_idx", 
    unique: true,
    background: true 
});

// Index cho các trường thường xuyên query
db.user.createIndex({ "role": 1 }, { 
    name: "role_idx", 
    background: true 
});

db.user.createIndex({ "createdAt": -1 }, { 
    name: "created_at_idx", 
    background: true 
});

db.user.createIndex({ "phone": 1 }, { 
    name: "phone_idx", 
    background: true 
});

// Indexes cho refresh_tokens collection
db.refresh_tokens.createIndex({ "token": 1 }, { 
    name: "token_unique_idx", 
    unique: true,
    background: true 
});

db.refresh_tokens.createIndex({ "expiryDate": 1 }, { 
    name: "expiry_date_idx", 
    expireAfterSeconds: 0,
    background: true 
});

db.refresh_tokens.createIndex({ "userId": 1 }, { 
    name: "user_id_idx", 
    background: true 
});

print('✓ Đã tạo xong indexes');

// ==================== KHỞI TẠO ADMIN USER ====================
print('3. Đang khởi tạo admin user...');

const currentTime = new Date();

db.user.insertOne({
    _id: ObjectId("651b12345678901234567891"),
    name: "System Administrator",
    email: "admin@hotelbooking.com",
    password: "$2a$10$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz012345", // password: admin123
    phone: "+84123456789",
    address: "123 Admin Street, Hanoi, Vietnam",
    cccd: "001100110011",
    role: "ADMIN", // Phải khớp với enum UserRole
    createdAt: currentTime,
    updatedAt: currentTime
});

print('✓ Đã khởi tạo admin user');

// ==================== KHỞI TẠO SAMPLE USERS ====================
print('4. Đang khởi tạo sample users...');

const sampleUsers = [
    {
        _id: ObjectId("651b12345678901234567892"),
        name: "John Doe",
        email: "john.doe@example.com",
        password: "$2a$10$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz012345", // password: user123
        phone: "+84123456780",
        address: "456 Main Street, Ho Chi Minh City, Vietnam",
        cccd: "001100110012",
        role: "USER",
        createdAt: new Date("2024-01-15T10:00:00Z"),
        updatedAt: new Date("2024-01-15T10:00:00Z")
    },
    {
        _id: ObjectId("651b12345678901234567893"),
        name: "Jane Smith",
        email: "jane.smith@example.com",
        password: "$2a$10$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz012345",
        phone: "+84123456781",
        address: "789 Park Avenue, Danang, Vietnam",
        cccd: "001100110013",
        role: "USER",
        createdAt: new Date("2024-01-16T11:30:00Z"),
        updatedAt: new Date("2024-01-16T11:30:00Z")
    },
    {
        _id: ObjectId("651b12345678901234567894"),
        name: "Hotel Manager",
        email: "manager@luxuryhotel.com",
        password: "$2a$10$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz012345",
        phone: "+84123456782",
        address: "321 Hotel Street, Nha Trang, Vietnam",
        cccd: "001100110014",
        role: "HOTEL_MANAGER",
        createdAt: new Date("2024-01-10T09:15:00Z"),
        updatedAt: new Date("2024-01-10T09:15:00Z")
    },
    {
        _id: ObjectId("651b12345678901234567895"),
        name: "Nguyen Van A",
        email: "nguyenvana@example.com",
        password: "$2a$10$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz012345",
        phone: "+84987654321",
        address: "111 Le Loi Street, Hanoi, Vietnam",
        cccd: "001100110015",
        role: "USER",
        createdAt: new Date("2024-01-20T14:20:00Z"),
        updatedAt: new Date("2024-01-20T14:20:00Z")
    },
    {
        _id: ObjectId("651b12345678901234567896"),
        name: "Tran Thi B",
        email: "tranthib@example.com",
        password: "$2a$10$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz012345",
        phone: "+84987654322",
        address: "222 Nguyen Hue Street, HCMC, Vietnam",
        cccd: "001100110016",
        role: "USER",
        createdAt: new Date("2024-01-18T16:45:00Z"),
        updatedAt: new Date("2024-01-18T16:45:00Z")
    },

];

db.user.insertMany(sampleUsers);

print('✓ Đã khởi tạo sample users');

// ==================== KHỞI TẠO REFRESH TOKENS (MẪU) ====================
print('5. Đang khởi tạo sample refresh tokens...');

db.refresh_tokens.insertMany([
    {
        _id: ObjectId("651d12345678901234567891"),
        token: "sample_refresh_token_1",
        userId: ObjectId("651b12345678901234567891"),
        expiryDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), // 7 days from now
        createdAt: new Date()
    },
    {
        _id: ObjectId("651d12345678901234567892"),
        token: "sample_refresh_token_2",
        userId: ObjectId("651b12345678901234567892"),
        expiryDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
        createdAt: new Date()
    }
]);

print('✓ Đã khởi tạo sample refresh tokens');

// ==================== TẠO VALIDATION RULES ====================
print('6. Đang tạo validation rules...');

db.runCommand({
    collMod: "user",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["name", "email", "password", "role"],
            properties: {
                name: {
                    bsonType: "string",
                    description: "Name must be a string and is required"
                },
                email: {
                    bsonType: "string",
                    pattern: "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                    description: "Email must be a valid email address"
                },
                password: {
                    bsonType: "string",
                    minLength: 6,
                    description: "Password must be at least 6 characters"
                },
                phone: {
                    bsonType: "string",
                    description: "Phone must be a string"
                },
                address: {
                    bsonType: "string",
                    description: "Address must be a string"
                },
                cccd: {
                    bsonType: "string",
                    description: "CCCD must be a string"
                },
                role: {
                    enum: ["ADMIN", "USER", "HOTEL_MANAGER"],
                    description: "Role must be one of: ADMIN, USER, HOTEL_MANAGER"
                },
                createdAt: {
                    bsonType: "date",
                    description: "CreatedAt must be a date"
                },
                updatedAt: {
                    bsonType: "date",
                    description: "UpdatedAt must be a date"
                }
            }
        }
    },
    validationLevel: "moderate"
});

print('✓ Đã tạo validation rules');

// ==================== TẠO VIEWS ====================
print('7. Đang tạo views...');

// View để lấy thông tin user cơ bản (không bao gồm password)
db.createView("user_info", "user", [
    {
        $project: {
            name: 1,
            email: 1,
            phone: 1,
            address: 1,
            cccd: 1,
            role: 1,
            createdAt: 1,
            updatedAt: 1
        }
    }
]);

// View để thống kê user theo role
db.createView("user_stats_by_role", "user", [
    {
        $group: {
            _id: "$role",
            count: { $sum: 1 },
            latestUser: { $max: "$createdAt" }
        }
    },
    {
        $sort: { count: -1 }
    }
]);

print('✓ Đã tạo views');

// ==================== THỐNG KÊ ====================
print('8. Đang tạo thống kê...');

const stats = {
    usersCount: db.user.countDocuments(),
    usersByRole: db.user.aggregate([
        {
            $group: {
                _id: "$role",
                count: { $sum: 1 }
            }
        }
    ]).toArray(),
    refreshTokensCount: db.refresh_tokens.countDocuments()
};

print('=== THỐNG KÊ KHỞI TẠO ===');
print(`- Tổng số users: ${stats.usersCount}`);
print(`- Tổng số refresh tokens: ${stats.refreshTokensCount}`);
print('- Phân bố users theo role:');
stats.usersByRole.forEach(roleStat => {
    print(`  + ${roleStat._id}: ${roleStat.count} users`);
});

print('=== HOÀN TẤT KHỞI TẠO MONGODB ===');
print('Database: hotelbooking');
print('Collections: user, refresh_tokens');
print('✅ MongoDB đã sẵn sàng sử dụng!');