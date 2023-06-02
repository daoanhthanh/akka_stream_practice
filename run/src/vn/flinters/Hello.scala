package vn.flinters

import com.google.cloud.datastore.{Datastore, DatastoreOptions, DatastoreReader, Key}

import scala.util.Properties

object Hello {
  Properties.setProp("scala.time", "true")
  private val start = System.currentTimeMillis()
  private val id = "1018981258214"
  private val url = "https://obs.line-scdn.net/0hSyXl98KTDFZlFSTNaw9zAV1FBydWeBtVBW0eYBVJPGZNLU0FWXVDMEAXVmRLJkMIXnFAMwUUAGRBJhsCDA"

  private val datastore: Datastore = DatastoreOptions.getDefaultInstance.getService
  private val idKeyFactory = datastore.newKeyFactory().setNamespace("creative-storage-dev").setKind("downloaded")
  private val entity = Option(datastore.asInstanceOf[DatastoreReader].get(key))
  private val end = System.currentTimeMillis()

  println(entity.map(_.getString("contentHash")).getOrElse("No entity found"))

  private def key: Key = idKeyFactory.newKey(id)
  println(s"Time taken: ${end - start} ms")
}