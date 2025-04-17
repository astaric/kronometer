//
//  ApiManager.swift
//  Kronometer
//
//  Created by Anze Staric on 16. 4. 25.
//

import Foundation

class ApiManager {
    let apiService: ApiService
    let authService: AuthService
    var selectedCompetitionId: ApiService.Competition.ID? {
        didSet {
            UserDefaults.standard.set(selectedCompetitionId, forKey: "selectedCompetitionId")
        }
    }

    static let shared = ApiManager()

    init(apiService: ApiService = .shared, authService: AuthService = .shared) {
        self.apiService = apiService
        self.authService = authService
        self.selectedCompetitionId =
            UserDefaults.standard.value(forKey: "selectedCompetitionId")
            as? ApiService.Competition.ID
    }
}

extension ApiManager {  // Authenticate
    var isAuthenticated: Bool {
        authService.token != nil
    }

    func login(authenticateHandler: @escaping AuthService.AuthenticateHandler) async throws {
        authService.token = try await authService.login(authenticateHandler: authenticateHandler)
    }

    func logout() async throws {
        if let token = authService.token {
            authService.token = nil
            await authService.revoke(token: token)
        }
    }
}

extension ApiManager {  // Competition
    func getNonArchivedCompetitions() async throws -> [ApiService.Competition] {
        guard let token = try await authService.validAccessToken() else {
            throw AuthError.missingToken
        }
        return try await apiService.getCompetitions(accessToken: token.accessToken).filter {
            $0.archived == false
        }
    }

    func getBikers() async throws -> [ApiService.Biker] {
        guard let token = try await authService.validAccessToken() else {
            throw AuthError.missingToken
        }
        guard let selectedCompetitionId else { throw ApiError.noCompetitionSelected }
        return try await apiService.getBikers(
            competitionId: selectedCompetitionId, accessToken: token.accessToken)
    }

    func updateTimes(for biker: Biker, startTime: Date? = nil, endTime: Date? = nil) async throws {
        guard let token = try await authService.validAccessToken() else {
            throw AuthError.missingToken
        }
        return try await apiService.updateTimes(
            competitionId: biker.competition_id,
            bikerNumber: biker.id,
            startTime: startTime,
            endTime: endTime,
            accessToken: token.accessToken
        )
    }
}
