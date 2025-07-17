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
import cats.data.NonEmptySet
import connectors.ReferenceDataConnector
import models.reference.{CountryCode, CustomsOffice}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomsOfficesServiceSpec extends SpecBase with BeforeAndAfterEach {

  val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  val service                                      = new CustomsOfficesService(mockRefDataConnector)

  val customsOffice1: CustomsOffice              = CustomsOffice("GB1", "BOSTON", "GB")
  val customsOffice2: CustomsOffice              = CustomsOffice("GB2", "Appledore", "GB")
  val customsOffices: NonEmptySet[CustomsOffice] = NonEmptySet.of(customsOffice1, customsOffice2)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "CustomsOfficesService" - {

    "getCustomsOfficesOfTransitForCountry" - {
      "must return a list of sorted customs offices of transit for a given country" in {

        when(mockRefDataConnector.getCustomsOfficesForCountryAndRole(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(customsOffices)))

        service.getCustomsOfficesOfTransitForCountry(CountryCode("GB")).futureValue.values mustEqual
          Seq(customsOffice2, customsOffice1)

        verify(mockRefDataConnector).getCustomsOfficesForCountryAndRole(eqTo("GB"), eqTo("TRA"))(any(), any())
      }
    }

    "getCustomsOfficesOfDestinationForCountry" - {
      "must return a list of sorted customs offices of destination for a given country" in {

        when(mockRefDataConnector.getCustomsOfficesForCountryAndRole(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(customsOffices)))

        service.getCustomsOfficesOfDestinationForCountry(CountryCode("GB")).futureValue.values mustEqual
          Seq(customsOffice2, customsOffice1)

        verify(mockRefDataConnector).getCustomsOfficesForCountryAndRole(eqTo("GB"), eqTo("DES"))(any(), any())
      }
    }

    "getCustomsOfficesOfExitForCountry" - {
      "must return a list of sorted customs offices of exit for a given country" in {

        when(mockRefDataConnector.getCustomsOfficesForCountryAndRole(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(customsOffices)))

        service.getCustomsOfficesOfExitForCountry(CountryCode("GB")).futureValue.values mustEqual
          Seq(customsOffice2, customsOffice1)

        verify(mockRefDataConnector).getCustomsOfficesForCountryAndRole(eqTo("GB"), eqTo("EXT"))(any(), any())
      }
    }

    "getCustomsOfficesOfDepartureForCountry" - {
      "must return a list of sorted customs offices of departure for a given country" in {

        when(mockRefDataConnector.getCustomsOfficesForCountryAndRole(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(customsOffices)))

        service.getCustomsOfficesOfDepartureForCountry("GB").futureValue.values mustEqual
          Seq(customsOffice2, customsOffice1)

        verify(mockRefDataConnector).getCustomsOfficesForCountryAndRole(eqTo("GB"), eqTo("DEP"))(any(), any())
      }
    }
  }
}
