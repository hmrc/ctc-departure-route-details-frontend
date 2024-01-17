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

package models

import cats.data.ReaderT
import models.journeyDomain.OpsError.ReaderError
import models.journeyDomain.ReaderSuccess
import pages.sections.Section
import pages.{InferredPage, Page, ReadOnlyPage}
import play.api.libs.json.{JsArray, Reads}
import queries.Gettable

package object domain {

  type EitherType[A]        = Either[ReaderError, A]
  type UserAnswersReader[A] = ReaderT[EitherType, UserAnswers, ReaderSuccess[A]]

  object UserAnswersReader {
    def apply[A](implicit ev: UserAnswersReader[A]): UserAnswersReader[A] = ev

    def apply[A](fn: UserAnswers => EitherType[ReaderSuccess[A]]): UserAnswersReader[A] =
      ReaderT[EitherType, UserAnswers, ReaderSuccess[A]](fn)

    def success[A](a: A): Read[A] = pages => {
      val fn: UserAnswers => EitherType[ReaderSuccess[A]] = _ => Right(ReaderSuccess(a, pages))
      apply(fn)
    }

    def none[A]: Read[Option[A]] = pages => success[Option[A]](None).apply(pages)

    def emptyList[A]: Read[Seq[A]] = pages => success[Seq[A]](Seq.empty[A]).apply(pages)

    def error[A](page: Gettable[_], message: Option[String] = None): Read[A] = pages => {
      val fn: UserAnswers => EitherType[ReaderSuccess[A]] = _ => Left(ReaderError(page, pages.append(page), message))
      apply(fn)
    }

    def readInferred[A](page: Gettable[A], inferredPage: Gettable[A])(implicit reads: Reads[A]): Read[A] =
      inferredPage.optionalReader.apply(_).flatMap {
        case ReaderSuccess(Some(value), pages) => UserAnswersReader.success(value).apply(pages)
        case ReaderSuccess(None, pages)        => page.reader.apply(pages)
      }
  }

  implicit class GettableAsFilterForNextReaderOps[A: Reads](a: Gettable[A]) {

    /**
      * Returns UserAnswersReader[B], where UserAnswersReader[B] which is run only if UserAnswerReader[A]
      * is defined and satisfies the predicate, if it defined and does not satisfy the predicate overall reader will
      * will fail returning a ReaderError. If the result of UserAnswerReader[A] is not defined then the overall reader will fail and
      * `next` will not be run
      */
    def filterMandatoryDependent[B](predicate: A => Boolean)(next: => Read[B]): Read[B] = pages =>
      a.reader(s"Reader for ${a.path} failed before reaching predicate")
        .apply(pages)
        .flatMap {
          case ReaderSuccess(x, pages) =>
            if (predicate(x)) {
              next(pages)
            } else {
              UserAnswersReader.error[B](a, Some(s"Mandatory predicate failed for ${a.path}")).apply(pages.append(a))
            }
        }

    /**
      * Returns UserAnswersReader[Option[B]], where UserAnswersReader[B] which is run only if UserAnswerReader[A]
      * is defined and satisfies the predicate, if it defined and does not satisfy the predicate overall reader will
      * will return None. If the result of UserAnswerReader[A] is not defined then the overall reader will fail and
      * `next` will not be run
      */
    def filterOptionalDependent[B](predicate: A => Boolean)(next: => Read[B]): Read[Option[B]] = pages =>
      a.reader(s"Reader for ${a.path} failed before reaching predicate")
        .apply(pages)
        .flatMap {
          case ReaderSuccess(x, pages) =>
            if (predicate(x)) {
              next(pages).map(_.to(Some(_)))
            } else {
              UserAnswersReader.success[Option[B]](None).apply(pages)
            }
        }
  }

  implicit class GettableAsReaderOps[A](a: Gettable[A]) {

    /**
      * Returns a reader for [[Gettable]], which will succeed with an [[A]]  if the value is defined
      * and will fail if it is not defined
      */

    def reader(implicit reads: Reads[A]): Read[A] = reader(None)

    def reader(message: String)(implicit reads: Reads[A]): Read[A] = reader(Some(message))

    private def reader(message: Option[String])(implicit reads: Reads[A]): Read[A] = pages => {
      val fn: UserAnswers => EitherType[ReaderSuccess[A]] = _.get(a) match {
        case Some(value) => Right(ReaderSuccess(value, pages.append(a)))
        case None        => Left(ReaderError(a, pages.append(a), message))
      }
      UserAnswersReader(fn)
    }

    def optionalReader(implicit reads: Reads[A]): Read[Option[A]] = pages => {
      val fn: UserAnswers => EitherType[ReaderSuccess[Option[A]]] = ua => Right(ReaderSuccess(ua.get(a), pages))
      UserAnswersReader(fn)
    }
  }

  implicit class JsArrayGettableAsReaderOps(jsArray: Gettable[JsArray]) {

    def arrayReader(implicit reads: Reads[JsArray]): Read[JsArray] = pages => {
      val fn: UserAnswers => EitherType[ReaderSuccess[JsArray]] =
        ua => Right(ReaderSuccess(ua.get(jsArray).getOrElse(JsArray()), pages.append(jsArray)))

      UserAnswersReader(fn)
    }

    def fieldReader[T](page: Index => Gettable[T])(implicit rds: Reads[T]): Read[Seq[T]] = pages => {
      val fn: UserAnswers => EitherType[ReaderSuccess[Seq[T]]] = ua => {
        Right {
          ua.get(jsArray).getOrElse(JsArray()).value.indices.foldLeft[ReaderSuccess[Seq[T]]](ReaderSuccess(Nil, pages)) {
            case (ReaderSuccess(ts, pages), i) =>
              val gettable = page(Index(i))
              ua.get(gettable) match {
                case Some(t) => ReaderSuccess(ts :+ t, pages.append(gettable))
                case None    => ReaderSuccess(ts, pages.append(gettable))
              }
          }
        }
      }
      UserAnswersReader(fn)
    }
  }

  type Pages   = Seq[Page]
  type Read[T] = Pages => UserAnswersReader[T]

  implicit class RichPages(pages: Pages) {

    def append(page: Page): Pages =
      page match {
        case _: Section[_]             => pages
        case _: InferredPage[_]        => pages
        case _: ReadOnlyPage[_]        => pages
        case _ if pages.contains(page) => pages
        case _                         => pages :+ page
      }
  }

  implicit class RichRead[A](value: Read[A]) {
    def map[B](f: ReaderSuccess[A] => ReaderSuccess[B]): Read[B] = value.apply(_).map(f)
  }

  implicit class RichTuple2[A, B](value: (Read[A], Read[B])) {

    def map[T](f: (A, B) => T): Read[T] =
      pmap {
        case (a, b) => pages => UserAnswersReader.success(f(a, b)).apply(pages)
      }

    def pmap[T](f: (A, B) => Read[T]): Read[T] = pages =>
      for {
        a      <- value._1(pages)
        b      <- value._2(a.pages)
        reader <- f(a.value, b.value)(b.pages)
      } yield reader
  }

  implicit class RichTuple3[A, B, C](value: (Read[A], Read[B], Read[C])) {

    def map[T](f: (A, B, C) => T): Read[T] =
      pmap {
        case (a, b, c) => pages => UserAnswersReader.success(f(a, b, c)).apply(pages)
      }

    def pmap[T](f: (A, B, C) => Read[T]): Read[T] = pages =>
      for {
        a      <- value._1(pages)
        b      <- value._2(a.pages)
        c      <- value._3(b.pages)
        reader <- f(a.value, b.value, c.value)(c.pages)
      } yield reader
  }

  implicit class RichTuple4[A, B, C, D](value: (Read[A], Read[B], Read[C], Read[D])) {

    def map[T](f: (A, B, C, D) => T): Read[T] =
      pmap {
        case (a, b, c, d) => pages => UserAnswersReader.success(f(a, b, c, d)).apply(pages)
      }

    def pmap[T](f: (A, B, C, D) => Read[T]): Read[T] = pages =>
      for {
        a      <- value._1(pages)
        b      <- value._2(a.pages)
        c      <- value._3(b.pages)
        d      <- value._4(c.pages)
        reader <- f(a.value, b.value, c.value, d.value)(d.pages)
      } yield reader
  }
}
