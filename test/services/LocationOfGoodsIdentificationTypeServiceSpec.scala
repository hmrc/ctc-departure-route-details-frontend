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

package services

import base.SpecBase
import config.Constants.LocationOfGoodsIdentifier._
import config.Constants.LocationType._
import connectors.ReferenceDataConnector
import models.{LocationOfGoodsIdentification, LocationType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LocationOfGoodsIdentificationTypeServiceSpec extends SpecBase with BeforeAndAfterEach {

  val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  val service                                      = new LocationOfGoodsIdentificationTypeService(mockRefDataConnector)

  private val unlocodeIdentifier: LocationOfGoodsIdentification      = LocationOfGoodsIdentification(UnlocodeIdentifier, "test1")
  private val customsOfficeIdentifier: LocationOfGoodsIdentification = LocationOfGoodsIdentification(CustomsOfficeIdentifier, "test2")
  private val eoriNumberIdentifier: LocationOfGoodsIdentification    = LocationOfGoodsIdentification(EoriNumberIdentifier, "test3")
  private val authorisationNumber: LocationOfGoodsIdentification     = LocationOfGoodsIdentification(AuthorisationNumberIdentifier, "test4")
  private val coordinatesIdentifier: LocationOfGoodsIdentification   = LocationOfGoodsIdentification(CoordinatesIdentifier, "test5")
  private val addressIdentifier: LocationOfGoodsIdentification       = LocationOfGoodsIdentification(AddressIdentifier, "test6")
  private val postalCode: LocationOfGoodsIdentification              = LocationOfGoodsIdentification(PostalCodeIdentifier, "test7")

  private val identifiers: Seq[LocationOfGoodsIdentification] =
    Seq(postalCode, unlocodeIdentifier, customsOfficeIdentifier, coordinatesIdentifier, eoriNumberIdentifier, authorisationNumber, addressIdentifier)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "LocationOfGoodsIdentificationTypeService" - {

    "getLocationOfGoodsIdentificationTypes" - {
      "must return a filtered list of LocationOfGoodsIdentification" - {
        "When LocationType is DesignatedLocation" in {

          when(mockRefDataConnector.getQualifierOfTheIdentifications()(any(), any()))
            .thenReturn(Future.successful(identifiers))
          val locationType = LocationType(DesignatedLocation, "Designated location")
          service.getLocationOfGoodsIdentificationTypes(locationType).futureValue mustBe Seq(unlocodeIdentifier, customsOfficeIdentifier)
        }

        "When LocationType is Authorised Place" in {

          when(mockRefDataConnector.getQualifierOfTheIdentifications()(any(), any()))
            .thenReturn(Future.successful(identifiers))
          val locationType = LocationType(AuthorisedPlace, "Authorised place")
          service.getLocationOfGoodsIdentificationTypes(locationType).futureValue mustBe Seq(authorisationNumber)
        }

        "When LocationType is Approved Place" in {

          when(mockRefDataConnector.getQualifierOfTheIdentifications()(any(), any()))
            .thenReturn(Future.successful(identifiers))
          val locationType = LocationType(ApprovedPlace, "Approved place")
          service.getLocationOfGoodsIdentificationTypes(locationType).futureValue mustBe Seq(
            postalCode,
            unlocodeIdentifier,
            coordinatesIdentifier,
            eoriNumberIdentifier,
            addressIdentifier
          )
        }

        "When LocationType is Other" in {

          when(mockRefDataConnector.getQualifierOfTheIdentifications()(any(), any()))
            .thenReturn(Future.successful(identifiers))
          val locationType = LocationType(Other, "Other")
          service.getLocationOfGoodsIdentificationTypes(locationType).futureValue mustBe Seq(
            postalCode,
            unlocodeIdentifier,
            coordinatesIdentifier,
            addressIdentifier
          )
        }
      }
    }
  }
}
