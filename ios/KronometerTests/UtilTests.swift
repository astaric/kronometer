//
//  UtilTests.swift
//  KronometerTests
//
//  Created by Anze Staric on 8. 6. 24.
//

import XCTest

final class UtilTests: XCTestCase {
    func testSelectNextCycle() throws {
        let arr = [1, 2, 3, 4, 5]
        let el = arr.selectNext(after: nil, predicate: { $0 % 2 == 0 })
        XCTAssert(el == 2)
    }

    func testNoMatching() throws {
        let arr = [1, 2, 3, 4, 5]
        let el = arr.selectNext(after: nil, predicate: { $0 % 10 == 0 })
        XCTAssert(el == nil)
    }

}
