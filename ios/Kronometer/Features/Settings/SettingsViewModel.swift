//
//  SettingsViewModel.swift
//  Kronometer
//
//  Created by Anze Staric on 17. 4. 25.
//

import SwiftUI

@Observable
class SettingsViewModel {
    var isAuthenticated: Bool
    var competitions: [ApiService.Competition] = []
    var competitionsLoaded: Bool = false
    var selectedCompetitionId: ApiService.Competition.ID? {
        didSet {
            apiManager.selectedCompetitionId = selectedCompetitionId
        }
    }
    
    var hasError: Bool = false
    var errorMessage: String = ""
    
    
    let apiManager: ApiManager
    init(apiManager: ApiManager = .shared) {
        self.apiManager = apiManager
        self.isAuthenticated = apiManager.isAuthenticated
        self.selectedCompetitionId = apiManager.selectedCompetitionId
    }
    
    func login(authenticateHandler: @escaping AuthService.AuthenticateHandler) async {
        do {
            try await apiManager.login(authenticateHandler: authenticateHandler)
            isAuthenticated = apiManager.isAuthenticated
            competitionsLoaded = false
            try await updateCompetitions()
        } catch {
            handleError(error)
        }
    }
    
    func logout() async {
        do {
            try await apiManager.logout()
            isAuthenticated = apiManager.isAuthenticated
        } catch {
            handleError(error)
        }
            
    }
    
    func refresh() async {
        do {
            try await updateCompetitions()
        } catch {
            handleError(error)
        }
    }
    
    private func updateCompetitions() async throws {
        competitionsLoaded = false
        if apiManager.isAuthenticated {
            competitions = try await apiManager.getNonArchivedCompetitions()
            competitionsLoaded = true
        }
    }
    
    private func handleError(_ error: Error) {
        self.errorMessage = error.localizedDescription
        self.hasError = true
    }
}
