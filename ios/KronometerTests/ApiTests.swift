//
//  ApiTests.swift
//  KronometerTests
//
//  Created by Anze Staric on 8. 6. 24.
//

import XCTest

final class ApiTests: XCTestCase {
    func testParseBikers() throws {
        let data = """
[{"model": "biker.biker", "pk": 378, "fields": {"number": 1, "name": "Neža", "surname": "Centa", "category": 38, "birth_year": null, "domestic": true, "start_time": "2023-06-18T14:59:07.210Z", "end_time": "2023-06-18T15:27:46.300Z"}}]
""".data(using: .utf8)
        let biker = try KronometerApi.parseBikers(data: data!).first
        
        XCTAssertEqual(biker?.number, 1)
        XCTAssertEqual(biker?.name, "Neža")
        XCTAssertEqual(biker?.surname, "Centa")
        XCTAssertEqual(biker?.start_time?.formatted(.dateTime.hour().minute().second()), "16:59:07")
    }
    
    func testParseBikersStartTimeNoMillis() throws {
        let data = """
[ {"model": "biker.biker", "pk": 423, "fields": {"number": 46, "name": "Aljaž", "surname": "Jaklič", "category": 43, "birth_year": null, "domestic": true, "start_time": "2023-06-18T15:24:26Z", "end_time": "2023-06-18T15:34:47Z"}}]
""".data(using: .utf8)
        let biker = try KronometerApi.parseBikers(data: data!).first
        
        XCTAssertEqual(biker?.number, 46)
        XCTAssertEqual(biker?.name, "Aljaž")
        XCTAssertEqual(biker?.surname, "Jaklič")
        XCTAssertEqual(biker?.start_time?.formatted(.dateTime.hour().minute().second()), "17:24:26")
        
    }
    
    func testParseBikersNoStartTime() throws {
        let data = """
[ {"model": "biker.biker", "pk": 423, "fields": {"number": 46, "name": "Aljaž", "surname": "Jaklič", "category": 43, "birth_year": null, "domestic": true, "start_time": null, "end_time": "2023-06-18T15:34:47Z"}}]
""".data(using: .utf8)
        let biker = try KronometerApi.parseBikers(data: data!).first
        
        XCTAssert(biker?.number == 46)
        XCTAssert(biker?.name == "Aljaž")
        XCTAssert(biker?.surname == "Jaklič")
        XCTAssert(biker?.start_time == nil)
        
    }
}
