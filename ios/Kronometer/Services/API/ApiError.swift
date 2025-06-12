//
//  ApiError.swift
//  Kronometer
//
//  Created by Anze Staric on 18. 4. 25.
//
import Foundation

enum ApiError: Error {
    case loginCancelled
    case missingToken
    case noCompetitionSelected
    case invalidRequest(_ message: String)
    case invalidResponse(_ message: String)
    case serverError(_ statusCode: Int?, _ error: String)
}

extension ApiError: LocalizedError {
    var errorDescription: String? {
        switch self {
        case .loginCancelled:
            return String(localized: "error_login_cancelled")
        case .missingToken:
            return String(localized: "error_missing_token")
        case .noCompetitionSelected:
            return String(localized: "error_select_competition")
        case let .invalidRequest(message):
            return String(
                localized: "error_invalid_request", defaultValue: "Invalid request: \(message)")
        case let .invalidResponse(message):
            return String(
                localized: "error_invalid_response",
                defaultValue: "Invalid server response: \(message)")
        case let .serverError(_, error):
            return error
        }
    }
}

extension ApiError: Equatable {
    static func == (lhs: ApiError, rhs: ApiError) -> Bool {
        switch (lhs, rhs) {
        case (.loginCancelled, .loginCancelled):
            return true
        case (.missingToken, .missingToken):
            return true
        case (.noCompetitionSelected, .noCompetitionSelected):
            return true
        case let (.invalidRequest(l), .invalidRequest(r)):
            return l == r
        case let (.invalidResponse(l), .invalidResponse(r)):
            return l == r
        case let (.serverError(lc, lm), .serverError(rc, rm)):
            return lc == rc && lm == rm
        default:
            return false
        }
    }
}
