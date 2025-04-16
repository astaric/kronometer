//
//  Settings.swift
//  Kronometer
//
//  Created by Anze Staric on 14. 6. 24.
//

import SwiftUI

struct Settings: View {
    @Environment(CountdownCounter.self) var countdown
    @Environment(BikerStore.self) var bikerStore
    @Environment(\.webAuthenticationSession) private var webAuthenticationSession
    
    @State var viewModel = SettingsViewModel()
    
    @State var showRemoveData = false
    @State var showRemoveDataConfirmation = false
    
    var body: some View {
        Form {
            startSection
            dataSection
            if (viewModel.isAuthenticated) {
                logoutButton
            }
        }
        .alert(isPresented: $viewModel.hasError) {
            Alert(
                title: Text(String(localized: "error_title")),
                message: Text(viewModel.errorMessage)
            )
        }
        .task {
            await viewModel.refresh()
        }
    }
    
    var startSection: some View {
        Section(String(localized: "start")) {
            @Bindable var binding = countdown
            Stepper(String(localized: "time_between_contestants", defaultValue: "Time between contestants: \(self.countdown.defaultCountdown) seconds"), value: $binding.defaultCountdown)
        }
    }
    
    var dataSection: some View {
        Section(String(localized: "section_data_title")) {
            if viewModel.isAuthenticated {
                if viewModel.competitionsLoaded {
                    competitionPicker
                } else {
                    ProgressView(String(localized: "loading_competitions"))
                }
            } else {
                loginButton
            }
            
            NavigationLink(destination: UpdateLog()) {
                Text(String(localized: "synchronization"))
            }
            
            Toggle(isOn: $showRemoveData, label: {
                Text(String(localized: "delete_all_data"))
            })
            if showRemoveData {
                removeAllDataButton
            }
        }
    }
    
    var competitionPicker: some View {
        Picker(String(localized: "competition_picker_title"), selection: $viewModel.selectedCompetitionId) {
            Text(String(localized: "choose_competition")).tag(ApiService.Competition.ID?.none)
            ForEach(viewModel.competitions, id: \.self) { competition in
                Text(competition.name).tag(competition.id)
            }
        }
    }
    
    var loginButton: some View {
        Button(String(localized: "login_button")) {
            Task {
                await viewModel.login {
                    (authorizeUrl, callbackURLScheme) in
                    try await webAuthenticationSession.authenticate(
                        using: authorizeUrl,
                        callbackURLScheme: callbackURLScheme
                    )
                }
            }
        }
    }
    
    private var removeAllDataButton: some View {
        Button(String(localized: "delete_all_data"), role: .destructive) {
            showRemoveDataConfirmation = true
        }.confirmationDialog(
            String(localized: "are_you_sure"),
            isPresented: $showRemoveDataConfirmation) {
                Button(String(localized: "delete_all_data"), role: .destructive) {
                    Task {
                        bikerStore.removeAllData()
                        try await bikerStore.refresh()
                        showRemoveData = false
                    }
                }
                
            }
    }
    
    private var logoutButton: some View {
        Button(String(localized: "logout_button")) {
            Task {
                await viewModel.logout()
            }
        }
    }
}


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
            competitions = try await apiManager.getCompetitions()
            competitionsLoaded = true
        }
    }
    
    private func handleError(_ error: Error) {
        self.errorMessage = error.localizedDescription
        self.hasError = true
    }
}


#Preview {
    return Settings()
        .environment(CountdownCounter())
    
}
