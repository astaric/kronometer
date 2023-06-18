//
//  BikerError.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import Foundation

enum BikerError: Error {
    case batchInsertError
    case fetchError
    case pushError(error: Error)
    case serverError(error: String)
    case wrongDataFormat(error: Error)
}

extension BikerError: LocalizedError {
    var errorDescription: String? {
        switch self {
            case .batchInsertError:
                return NSLocalizedString("Could not execute batch insert request", comment: "")
            case .fetchError:
                return NSLocalizedString("Could not download biker data", comment: "")
            case .pushError(let error):
                return NSLocalizedString("Could not upload data: \(error.localizedDescription)", comment: "")
            case .serverError(let error):
                return NSLocalizedString("Server returned error: \(error)", comment: "")
            case .wrongDataFormat(let error):
                return NSLocalizedString("Could not parse json data: \(error.localizedDescription)", comment: "")
        }
    }
}
