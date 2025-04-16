import Foundation

final class KeychainManager {

    typealias ItemAttributes = [String: Any]
    typealias KeychainDictionary = [String: Any]

    static let shared = KeychainManager()
    private init() {}

    // MARK: - Public Methods

    func saveItem<T: Encodable>(
        _ item: T,
        itemClass: ItemClass,
        key: String,
        attributes: ItemAttributes? = nil
    ) throws {
        let itemData = try JSONEncoder().encode(item)
        let query = makeQuery(
            itemClass: itemClass,
            key: key,
            attributes: attributes,
            data: itemData
        )
        let result = SecItemAdd(query as CFDictionary, nil)

        guard result == errSecSuccess else {
            throw convertError(result)
        }
    }

    func retrieveItem<T: Decodable>(
        ofClass itemClass: ItemClass,
        key: String,
        attributes: ItemAttributes? = nil
    ) throws -> T {
        var query = makeQuery(
            itemClass: itemClass,
            key: key,
            attributes: attributes
        )
        query[kSecReturnAttributes as String] = true
        query[kSecReturnData as String] = true

        var item: CFTypeRef?
        let result = SecItemCopyMatching(query as CFDictionary, &item)
        guard result == errSecSuccess else {
            throw convertError(result)
        }

        guard
            let keychainItem = item as? [String: Any],
            let data = keychainItem[kSecValueData as String] as? Data
        else {
            throw KeychainError.invalidData
        }

        return try JSONDecoder().decode(T.self, from: data)
    }

    func updateItem<T: Encodable>(
        with item: T,
        ofClass itemClass: ItemClass,
        key: String,
        attributes: ItemAttributes? = nil
    ) throws {
        let itemData = try JSONEncoder().encode(item)
        let query = makeQuery(
            itemClass: itemClass,
            key: key,
            attributes: attributes
        )

        let updateData: KeychainDictionary = [
            kSecValueData as String: itemData
        ]

        let result = SecItemUpdate(
            query as CFDictionary,
            updateData as CFDictionary
        )
        guard result == errSecSuccess else {
            throw convertError(result)
        }
    }

    func deleteItem(
        ofClass itemClass: ItemClass,
        key: String,
        attributes: ItemAttributes? = nil
    ) throws {
        let query = makeQuery(
            itemClass: itemClass,
            key: key,
            attributes: attributes
        )

        let result = SecItemDelete(query as CFDictionary)
        guard result == errSecSuccess else {
            throw convertError(result)
        }
    }

    // MARK: - Private Helpers

    private func makeQuery(
        itemClass: ItemClass,
        key: String,
        attributes: ItemAttributes? = nil,
        data: Data? = nil
    ) -> KeychainDictionary {
        var query: KeychainDictionary = [
            kSecClass as String: itemClass.secClass,
            kSecAttrAccount as String: key,
        ]

        if let attributes {
            for (key, value) in attributes {
                query[key] = value
            }
        }

        if let data {
            query[kSecValueData as String] = data
        }

        return query

    }
}

extension KeychainManager {
    enum ItemClass: String {
        case generic = "kSecClassGenericPassword"
        case password = "kSecClassInternetPassword"
        case certificate = "kSecClassCertificate"
        case cryptography = "kSecClassKey"
        case identity = "kSecClassIdentity"

        init?(secClass: CFString) {
            switch secClass {
            case kSecClassGenericPassword: self = .generic
            case kSecClassInternetPassword: self = .password
            case kSecClassCertificate: self = .certificate
            case kSecClassKey: self = .cryptography
            case kSecClassIdentity: self = .identity
            default: return nil
            }
        }

        var secClass: CFString {
            switch self {
            case .generic: return kSecClassGenericPassword
            case .password: return kSecClassInternetPassword
            case .certificate: return kSecClassCertificate
            case .cryptography: return kSecClassKey
            case .identity: return kSecClassIdentity
            }
        }
    }
}

extension KeychainManager {
    enum KeychainError: LocalizedError {
        case invalidData
        case itemNotFound
        case duplicateItem
        case incorrectAttributeForClass
        case unexpected(OSStatus)

        var errorDescription: String? {
            switch self {
            case .invalidData:
                return "The data retrieved from the Keychain is invalid."
            case .itemNotFound:
                return "No item was found for the given key."
            case .duplicateItem:
                return "An item with the same key already exists."
            case .incorrectAttributeForClass:
                return
                    "The attribute provided is incorrect for the specified item class."
            case .unexpected(let status):
                if let message = SecCopyErrorMessageString(status, nil)
                    as String?
                {
                    return "Unexpected error: \(message) (\(status))"
                } else {
                    return "Unexpected error with status code: \(status)"
                }
            }
        }
    }

    /// Maps a raw OSStatus code from Keychain services to a `KeychainError`.
    private func convertError(_ error: OSStatus) -> KeychainError {
        switch error {
        case errSecItemNotFound:
            return .itemNotFound
        case errSecDataTooLarge:
            return .invalidData
        case errSecDuplicateItem:
            return .duplicateItem
        default:
            return .unexpected(error)
        }
    }
}

@propertyWrapper
struct KeychainBacked<Value: Codable> {
    private let key: String
    private let itemClass: KeychainManager.ItemClass
    private let attributes: KeychainManager.ItemAttributes?
    private var cachedValue: Value?

    private let manager: KeychainManager

    init(
        key: String,
        ofClass itemClass: KeychainManager.ItemClass = .generic,
        attributes: KeychainManager.ItemAttributes? = nil,
        manager: KeychainManager = .shared
    ) {
        self.key = key
        self.itemClass = itemClass
        self.attributes = attributes
        self.manager = manager
    }

    var wrappedValue: Value? {
        mutating get {
            if let cachedValue {
                return cachedValue
            }

            cachedValue = try? manager.retrieveItem(
                ofClass: itemClass,
                key: key,
                attributes: attributes
            )
            return cachedValue
        }

        mutating set {
            cachedValue = newValue
            if let newValue {
                try? manager.saveItem(
                    newValue,
                    itemClass: itemClass,
                    key: key,
                    attributes: attributes
                )

            } else {
                try? manager.deleteItem(
                    ofClass: itemClass,
                    key: key,
                    attributes: attributes
                )
            }
        }
    }
}
