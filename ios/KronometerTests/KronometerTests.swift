//
//  KronometerTests.swift
//  KronometerTests
//
//  Created by Anze Staric on 13/06/2023.
//

import XCTest

final class KronometerTests: XCTestCase {
    func testOne() throws {
        let data = Data("""
            [
              {
                "model": "biker.biker",
                "pk": 352,
                "fields": {
                  "number": 1,
                  "name": "Iztok",
                  "surname": "Merljak",
                  "category": 38,
                  "birth_year": null,
                  "domestic": false,
                  "start_time": "2022-09-22T06:55:25.858Z",
                  "end_time": "2022-09-22T06:58:02.253Z"
                }
              }
            ]
        """.utf8)
        /*
        let bikers = try parseBikersJson(jsonData: data)

        XCTAssert(bikers.count == 1)
        let biker = bikers.first!
        XCTAssert(biker.name == "Iztok")
        XCTAssert(biker.surname == "Merljak")

        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        let calendar = Calendar.current
        let expectedTime = formatter.date(from: "2022-09-22 08:55:25")!
        print(biker.start_time)
        print(expectedTime)
        XCTAssert(biker.start_time! > expectedTime)
        XCTAssert(biker.start_time! < calendar.date(byAdding: .second, value: 1, to: expectedTime)!)
    }

    func testMissingTimes() throws {
        let data = Data("""
        [{"model": "biker.biker", "pk": 371, "fields": {"number": 1, "name": "Test", "surname": "Prvi", "category": 42, "birth_year": null, "domestic": true, "start_time": null, "end_time": null}}, {"model": "biker.biker", "pk": 372, "fields": {"number": 2, "name": "Test", "surname": "Drugi", "category": 43, "birth_year": null, "domestic": true, "start_time": null, "end_time": null}}, {"model": "biker.biker", "pk": 373, "fields": {"number": 3, "name": "Test", "surname": "Tretji", "category": 44, "birth_year": null, "domestic": true, "start_time": null, "end_time": null}}]
        """.utf8)
        let bikers = try parseBikersJson(jsonData: data)
         */

    }
}
