//
//  DateComponentsFormatter.swift
//  Kronometer
//
//  Created by Anze Staric on 14/06/2023.
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
