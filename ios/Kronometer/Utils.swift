//
//  Utils.swift
//  Kronometer
//
//  Created by Anze Staric on 7. 6. 24.
//

import SwiftUI

extension Color {
    static let light = Color(red: 0.95, green: 0.95, blue: 0.95)
    static let dark = Color(red: 0.05, green: 0.05, blue: 0.05)
}

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

extension DateComponentsFormatter {
    func string(from: Date?, to: Date?) -> String? {
        guard let from = from,
              let to = to else {
            return nil
        }

        return self.string(from: from, to: to)
    }
}

extension Array {
    func selectNext(after idx: Self.Index?, predicate: (Self.Element) -> Bool) -> Element? {
        let idx = idx ?? self.endIndex
        var nextIdx = self.index(after: idx)
        if nextIdx >= self.endIndex {
            nextIdx = self.startIndex
        }
        while nextIdx != idx {
            if predicate(self[nextIdx]) {
                return self[nextIdx]
            }
            nextIdx = self.index(after: nextIdx)
            if nextIdx == idx {
                break
            }
            if nextIdx >= self.endIndex {
                nextIdx = self.startIndex
            }
        }
        return nil
    }
}
