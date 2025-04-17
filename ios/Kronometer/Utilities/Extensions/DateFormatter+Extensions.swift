//
//  Date+Extensions.swift
//  Kronometer
//
//  Created by Anze Staric on 17. 4. 25.
//

import Foundation

extension DateFormatter {
    static var hms: DateFormatter = {
        let fmt = DateFormatter()
        fmt.dateFormat = "HH:mm:ss"
        return fmt
    }()

    static var hmsms: DateFormatter = {
        let fmt = DateFormatter()
        fmt.dateFormat = "HH:mm:ss.SSS"
        return fmt
    }()



    static var jsonUtc: DateFormatter = {
        let fmt = DateFormatter()
        fmt.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        fmt.timeZone = TimeZone(abbreviation: "UTC")
        return fmt
    }()
}
