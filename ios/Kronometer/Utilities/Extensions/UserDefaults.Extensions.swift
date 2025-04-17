//
//  UserDefaults.Extensions.swift
//  Kronometer
//
//  Created by Anze Staric on 17. 4. 25.
//

import Foundation

extension UserDefaults {
    func codable<T: Codable>(forKey key: String) -> T? {
        guard let data = data(forKey: key) else { return nil }
        return try? JSONDecoder().decode(T.self, from: data)
    }
    func setCodable<T: Codable>(_ value: T, forKey key: String) {
        let data = try? JSONEncoder().encode(value)
        set(data, forKey: key)
    }
}
