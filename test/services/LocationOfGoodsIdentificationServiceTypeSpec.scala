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
import config.Constants._
import connectors.ReferenceDataConnector
import models.{Index, LocationOfGoodsIdentification, LocationType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import pages.locationOfGoods.LocationTypePage
import pages.sections.transit.OfficeOfTransitSection
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LocationOfGoodsIdentificationServiceTypeSpec extends SpecBase with BeforeAndAfterEach {

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
      "must return a list of sorted LocationOfGoodsIdentification" in {

        when(mockRefDataConnector.getQualifierOfTheIdentifications()(any(), any()))
          .thenReturn(Future.successful(identifiers))
        val answers = emptyUserAnswers.setValue(OfficeOfTransitSection(Index(0)), Json.obj("foo" -> "bar"))
        service.getLocationOfGoodsIdentificationTypes(answers).futureValue mustBe identifiers

      }
      "must return a filtered list of LocationOfGoodsIdentification dependant on answers to LocationTypePage" - {
        "When LocationType is DesignatedLocation" in {

          when(mockRefDataConnector.getQualifierOfTheIdentifications()(any(), any()))
            .thenReturn(Future.successful(identifiers))
          val answers = emptyUserAnswers
            .setValue(OfficeOfTransitSection(Index(0)), Json.obj("foo" -> "bar"))
            .setValue(LocationTypePage, LocationType(DesignatedLocation, "Designated location"))
          service.getLocationOfGoodsIdentificationTypes(answers).futureValue mustBe Seq(unlocodeIdentifier, customsOfficeIdentifier)
        }

        "When LocationType is Authorised Place" in {

          when(mockRefDataConnector.getQualifierOfTheIdentifications()(any(), any()))
            .thenReturn(Future.successful(identifiers))
          val answers = emptyUserAnswers
            .setValue(OfficeOfTransitSection(Index(0)), Json.obj("foo" -> "bar"))
            .setValue(LocationTypePage, LocationType(AuthorisedPlace, "Authorised place"))
          service.getLocationOfGoodsIdentificationTypes(answers).futureValue mustBe Seq(authorisationNumber)
        }

        "When LocationType is Approved Place" in {

          when(mockRefDataConnector.getQualifierOfTheIdentifications()(any(), any()))
            .thenReturn(Future.successful(identifiers))
          val answers = emptyUserAnswers
            .setValue(OfficeOfTransitSection(Index(0)), Json.obj("foo" -> "bar"))
            .setValue(LocationTypePage, LocationType(ApprovedPlace, "Approved place"))
          service.getLocationOfGoodsIdentificationTypes(answers).futureValue mustBe Seq(
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
          val answers = emptyUserAnswers
            .setValue(OfficeOfTransitSection(Index(0)), Json.obj("foo" -> "bar"))
            .setValue(LocationTypePage, LocationType(Other, "Other"))
          service.getLocationOfGoodsIdentificationTypes(answers).futureValue mustBe Seq(postalCode,
                                                                                        unlocodeIdentifier,
                                                                                        coordinatesIdentifier,
                                                                                        addressIdentifier
          )
        }

      }
    }
  }
}
