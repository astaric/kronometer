//
//  Settings.swift
//  Kronometer
//
//  Created by Anze Staric on 14. 6. 24.
//

import SwiftUI

struct Settings: View {
    @Environment(CountdownViewModel.self) var countdown
    @Environment(BikerStore.self) var bikerStore
    @Environment(\.webAuthenticationSession) private var webAuthenticationSession

    @State var viewModel = SettingsViewModel()

    @State var showRemoveData = false
    @State var showRemoveDataConfirmation = false

    var body: some View {
        Form {
            startSection
            dataSection
            if viewModel.isAuthenticated {
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
        .navigationTitle(String(localized: "settings"))
    }

    var startSection: some View {
        Section(String(localized: "start")) {
            @Bindable var binding = countdown
            Stepper(
                String(
                    localized: "time_between_contestants",
                    defaultValue:
                        "Time between contestants: \(self.countdown.defaultCountdown) seconds"),
                value: $binding.defaultCountdown)
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

            Toggle(
                isOn: $showRemoveData,
                label: {
                    Text(String(localized: "delete_all_data"))
                })
            if showRemoveData {
                removeAllDataButton
            }
        }
    }

    var competitionPicker: some View {
        Picker(
            String(localized: "competition_picker_title"),
            selection: $viewModel.selectedCompetitionId
        ) {
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
            isPresented: $showRemoveDataConfirmation
        ) {
            Button(String(localized: "delete_all_data"), role: .destructive) {
                Task {
                    bikerStore.removeAllData()
                    try await bikerStore.refresh()
                    await UpdateManager.shared.removeAllData()
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

#Preview {
    return Settings()
        .environment(CountdownViewModel())

}
