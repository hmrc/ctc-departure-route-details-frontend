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

package config

object Constants {

  object CountryCode {
    val GB = "GB"
    val AD = "AD"
  }

  object SpecificCircumstanceIndicator {
    val XXX = "XXX"
  }

  object AdditionalDeclarationType {
    val Standard = "A"
    val PreLodge = "D"
  }

  object DeclarationType {
    val TIR = "TIR"
    val T2  = "T2"
    val T   = "T"
  }

  object SecurityType {
    val NoSecurityDetails                             = "0"
    val EntrySummaryDeclarationSecurityDetails        = "1"
    val ExitSummaryDeclarationSecurityDetails         = "2"
    val EntryAndExitSummaryDeclarationSecurityDetails = "3"
  }

  object LocationOfGoodsIdentifier {
    val CustomsOfficeIdentifier       = "V"
    val EoriNumberIdentifier          = "X"
    val AuthorisationNumberIdentifier = "Y"
    val UnlocodeIdentifier            = "U"
    val CoordinatesIdentifier         = "W"
    val AddressIdentifier             = "Z"
    val PostalCodeIdentifier          = "T"
  }

  object LocationType {
    val DesignatedLocation = "A"
    val AuthorisedPlace    = "B"
    val ApprovedPlace      = "C"
    val Other              = "D"
  }
}
