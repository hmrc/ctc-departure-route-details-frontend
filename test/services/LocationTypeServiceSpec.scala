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
import models.ProcedureType
import models.reference.LocationType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LocationTypeServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new LocationTypeService(mockRefDataConnector)

  private val lt1 = LocationType("D", "Other")
  private val lt2 = LocationType("C", "Approved place")
  private val lt3 = LocationType("B", "Authorised place")
  private val lt4 = LocationType("A", "Designated location")
  private val lts = NonEmptySet.of(lt1, lt2, lt3, lt4)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "LocationTypeService" - {

    "getLocationTypes" - {
      "must return a list of sorted location types" - {
        "when normal procedure type" in {
          when(mockRefDataConnector.getTypesOfLocation()(any(), any()))
            .thenReturn(Future.successful(lts))

          service.getLocationTypes(ProcedureType.Normal).futureValue mustBe
            Seq(lt4, lt2, lt1)

          verify(mockRefDataConnector).getTypesOfLocation()(any(), any())
        }

        "when simplified procedure type" in {
          when(mockRefDataConnector.getTypesOfLocation()(any(), any()))
            .thenReturn(Future.successful(lts))

          service.getLocationTypes(ProcedureType.Simplified).futureValue mustBe
            Seq(lt3)

          verify(mockRefDataConnector).getTypesOfLocation()(any(), any())
        }
      }
    }
  }
}
