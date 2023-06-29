/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package generators

import models.AddressLine.{Country => _}
import models.domain.StringFieldRegex.{coordinatesLatitudeMaxRegex, coordinatesLongitudeMaxRegex}
import models.reference._
import models.{PostalCodeAddress, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.mvc.Call
import uk.gov.hmrc.http.HttpVerbs._
import wolfendale.scalacheck.regexp.RegexpGen

trait ModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryLocalReferenceNumber: Arbitrary[LocalReferenceNumber] =
    Arbitrary {
      for {
        lrn <- stringsWithMaxLength(22: Int, Gen.alphaNumChar)
      } yield new LocalReferenceNumber(lrn)
    }

  implicit lazy val arbitraryEoriNumber: Arbitrary[EoriNumber] =
    Arbitrary {
      for {
        number <- stringsWithMaxLength(17: Int)
      } yield EoriNumber(number)
    }

  implicit lazy val arbitraryMode: Arbitrary[Mode] = Arbitrary {
    Gen.oneOf(NormalMode, CheckMode)
  }

  implicit lazy val arbitraryIndex: Arbitrary[Index] = Arbitrary {
    for {
      position <- Gen.choose(0: Int, 10: Int)
    } yield Index(position)
  }

  implicit lazy val arbitraryCall: Arbitrary[Call] = Arbitrary {
    for {
      method <- Gen.oneOf(GET, POST)
      url    <- nonEmptyString
    } yield Call(method, url)
  }

  implicit lazy val arbitraryCountryCode: Arbitrary[CountryCode] =
    Arbitrary {
      Gen
        .pick(CountryCode.Constants.countryCodeLength, 'A' to 'Z')
        .map(
          code => CountryCode(code.mkString)
        )
    }

  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      for {
        code <- arbitrary[CountryCode]
        name <- nonEmptyString
      } yield Country(code, name)
    }

  implicit def arbitrarySelectableList[T <: Selectable](implicit arbitrary: Arbitrary[T]): Arbitrary[SelectableList[T]] = Arbitrary {
    for {
      values <- listWithMaxLength[T]()
    } yield SelectableList(values.distinctBy(_.value))
  }

  implicit lazy val arbitraryCustomsOffice: Arbitrary[CustomsOffice] =
    Arbitrary {
      for {
        id          <- nonEmptyString
        name        <- nonEmptyString
        phoneNumber <- Gen.option(Gen.alphaNumStr)
      } yield CustomsOffice(id, name, phoneNumber)
    }

  implicit lazy val arbitraryUnLocode: Arbitrary[UnLocode] =
    Arbitrary {
      for {
        unLocodeExtendedCode <- nonEmptyString
        name                 <- nonEmptyString
      } yield UnLocode(unLocodeExtendedCode, name)
    }

  implicit lazy val arbitraryLocationType: Arbitrary[LocationType] =
    Arbitrary {
      Gen.oneOf(LocationType.values)
    }

  implicit lazy val arbitraryLocationOfGoodsIdentification: Arbitrary[LocationOfGoodsIdentification] =
    Arbitrary {
      Gen.oneOf(LocationOfGoodsIdentification.values)
    }

  implicit lazy val arbitraryCoordinates: Arbitrary[Coordinates] =
    Arbitrary {
      for {
        latitude  <- RegexpGen.from(coordinatesLatitudeMaxRegex)
        longitude <- RegexpGen.from(coordinatesLongitudeMaxRegex)
      } yield Coordinates(latitude, longitude)
    }

  implicit lazy val arbitraryDynamicAddress: Arbitrary[DynamicAddress] =
    Arbitrary {
      for {
        numberAndStreet <- nonEmptyString
        city            <- nonEmptyString
        postalCode      <- Gen.option(nonEmptyString)
      } yield DynamicAddress(numberAndStreet, city, postalCode)
    }

  lazy val arbitraryDynamicAddressWithRequiredPostalCode: Arbitrary[DynamicAddress] =
    Arbitrary {
      for {
        numberAndStreet <- nonEmptyString
        city            <- nonEmptyString
        postalCode      <- nonEmptyString
      } yield DynamicAddress(numberAndStreet, city, Some(postalCode))
    }

  implicit lazy val arbitraryPostalCodeAddress: Arbitrary[PostalCodeAddress] =
    Arbitrary {
      for {
        streetNumber <- nonEmptyString
        postalCode   <- nonEmptyString
        country      <- arbitrary[Country]
      } yield PostalCodeAddress(streetNumber, postalCode, country)
    }

  implicit lazy val arbitraryDeclarationType: Arbitrary[DeclarationType] =
    Arbitrary {
      Gen.oneOf(DeclarationType.values)
    }

  lazy val arbitraryNonOption4DeclarationType: Arbitrary[DeclarationType] =
    Arbitrary {
      Gen.oneOf(DeclarationType.values.filterNot(_ == DeclarationType.Option4))
    }

  lazy val arbitraryXiCustomsOffice: Arbitrary[CustomsOffice] =
    Arbitrary {
      for {
        id          <- stringsWithMaxLength(stringMaxLength)
        name        <- stringsWithMaxLength(stringMaxLength)
        phoneNumber <- Gen.option(stringsWithMaxLength(stringMaxLength))
      } yield CustomsOffice(id, name, phoneNumber)
    }

  lazy val arbitraryGbCustomsOffice: Arbitrary[CustomsOffice] =
    Arbitrary {
      for {
        id          <- stringsWithMaxLength(stringMaxLength)
        name        <- stringsWithMaxLength(stringMaxLength)
        phoneNumber <- Gen.option(stringsWithMaxLength(stringMaxLength))
      } yield CustomsOffice(id, name, phoneNumber)
    }

  lazy val arbitraryOfficeOfDeparture: Arbitrary[CustomsOffice] =
    Arbitrary {
      Gen.oneOf(arbitraryGbCustomsOffice.arbitrary, arbitraryXiCustomsOffice.arbitrary)
    }

  implicit lazy val arbitrarySecurityDetailsType: Arbitrary[SecurityDetailsType] =
    Arbitrary {
      Gen.oneOf(SecurityDetailsType.values)
    }

  lazy val arbitrarySomeSecurityDetailsType: Arbitrary[SecurityDetailsType] =
    Arbitrary {
      Gen.oneOf(SecurityDetailsType.values.filterNot(_ == SecurityDetailsType.NoSecurityDetails))
    }

  lazy val arbitraryIncompleteTaskStatus: Arbitrary[TaskStatus] = Arbitrary {
    Gen.oneOf(TaskStatus.InProgress, TaskStatus.NotStarted, TaskStatus.CannotStartYet)
  }
}
