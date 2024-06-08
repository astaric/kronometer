//
//  StartHome.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import SwiftUI

struct StartHome: View {
    @Environment(\.verticalSizeClass) var verticalSizeClass
    @Environment(CountdownModel.self) var countdown
    @Environment(BikerStore.self) var bikerStore
    @EnvironmentObject var bleController: BLEController
    
    @State var showBikerList = false
    @State var selectedBiker: Biker?

    var body: some View {
        orientationDependentLayout
            .onChange(of: bikerStore.nextBikerOnStart) { oldBiker, newBiker in
                selectedBiker = bikerStore.nextBikerOnStart
            }
            .onAppear {
                if selectedBiker == nil {
                    if bikerStore.nextBikerOnStart == nil {
                        bikerStore.selectNextBikerToStart()
                    } else {
                        selectedBiker = bikerStore.nextBikerOnStart
                    }
                }
            }
    }
    
    @ViewBuilder
    var orientationDependentLayout: some View {
        if verticalSizeClass == .regular {
            VStack {
                Color.clear
                    .overlay {
                        Countdown()
                            .padding([.top, .bottom], 50)
                    }
                separator
                currentBiker
                    .frame(maxHeight: 100)
                separator
                startButton
                    .ignoresSafeArea()
            }
        } else {
            HStack {
                VStack() {
                    Color.clear
                        .overlay {
                            Countdown()
                        }
                    separator
                    currentBiker
                }
                startButton
                    .ignoresSafeArea()
                    .background(.red)
            }
        }
    }
    
    @ViewBuilder
    var currentBiker: some View {
        NavigationLink {
            StartList()
        } label: {
            bikerName
                .contentShape(Rectangle())
        }
    }
    
    @ViewBuilder
    var bikerName: some View {
        if let selectedBiker {
            StartListItem(biker: selectedBiker)
        } else {
            Text("No more bikers")
        }
    }
    
    var startButton: some View {
        Button {
            start()
        } label: {
            Text("Start")
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .font(.custom("", size: 50, relativeTo: .largeTitle))
                .foregroundColor(.primary)
        }
        .background(Color(UIColor.secondarySystemGroupedBackground))
        .onReceive(self.bleController.$events) { newValue in
            if countdown.timeRemaining <= 5 {
                start()
            }
        }
        .onChange(of: self.countdown.defaultCountdown) { _, _ in
            countdown.reset()
        }
    }
    
    var separator: some View {
        Rectangle()
            .foregroundColor(.secondary)
            .frame(height: 2)
    }

    private func start() {
        if let selectedBiker {
            bikerStore.setStartTime(for: selectedBiker)
        }
        countdown.reset()
    }
}

struct StartHome_Previews: PreviewProvider {
    static var previews: some View {
        let countdownModel = CountdownModel()
        countdownModel.reset()
        return NavigationStack {
            StartHome()
                .environment(countdownModel)
                .environmentObject(BLEController())
        }        
        .environment(BikerStore())
    }
}
