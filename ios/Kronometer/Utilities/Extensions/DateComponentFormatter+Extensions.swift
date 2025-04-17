//
//  DateComponentFormatter+Extensions.swift
//  Kronometer
//
//  Created by Anze Staric on 17. 4. 25.
//

import Foundation

extension DateComponentsFormatter {
    func string(from: Date?, to: Date?) -> String? {
        guard let from = from,
              let to = to else {
            return nil
        }

        return self.string(from: from, to: to)
    }
}
