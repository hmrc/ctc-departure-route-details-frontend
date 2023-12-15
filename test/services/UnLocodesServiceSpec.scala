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
import connectors.ReferenceDataConnector
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.reference.UnLocode
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnLocodesServiceSpec extends SpecBase with BeforeAndAfterEach {

  val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  val service                                      = new UnLocodesService(mockRefDataConnector)

  val unLocode1: UnLocode = UnLocode("ADALV", "Andorra la Vella")
  val unLocode2: UnLocode = UnLocode("ADCAN", "Canillo")
  val unLocodes           = Seq(unLocode1, unLocode2)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "UnLocodesService" - {

    "doesUnLocodeExist" - {
      "must return true" - {
        "when UN/LOCODE exists" in {

          when(mockRefDataConnector.getUnLocode(any())(any(), any()))
            .thenReturn(Future.successful(Seq(unLocode1)))

          service.doesUnLocodeExist(unLocode1.unLocodeExtendedCode).futureValue mustBe true

          verify(mockRefDataConnector).getUnLocode(eqTo(unLocode1.unLocodeExtendedCode))(any(), any())
        }
      }

      "must return false" - {
        "when UN/LOCODE does not exist" in {

          when(mockRefDataConnector.getUnLocode(any())(any(), any()))
            .thenReturn(Future.successful(Seq.empty))

          service.doesUnLocodeExist(unLocode1.unLocodeExtendedCode).futureValue mustBe false

          verify(mockRefDataConnector).getUnLocode(eqTo(unLocode1.unLocodeExtendedCode))(any(), any())
        }

        "when UN/LOCODE does not exist in reference data" in {

          val unLocode = "ABCDE"

          when(mockRefDataConnector.getUnLocode(any())(any(), any()))
            .thenReturn(Future.failed(new NoReferenceDataFoundException))

          service.doesUnLocodeExist(unLocode1.unLocodeExtendedCode).futureValue mustBe false

          verify(mockRefDataConnector).getUnLocode(eqTo(unLocode1.unLocodeExtendedCode))(any(), any())
        }
      }
    }
  }
}
