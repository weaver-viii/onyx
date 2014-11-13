(ns onyx.log.notify-watchers-test
  (:require [onyx.extensions :as extensions]
            [onyx.log.entry :refer [create-log-entry]]
            [midje.sweet :refer :all]))

(def entry (create-log-entry :notify-watchers {:observer :a :subject :d}))

(def f (extensions/apply-log-entry (:fn entry) (:args entry)))

(def rep-diff (partial extensions/replica-diff :notify-watchers))

(def rep-reactions (partial extensions/reactions :notify-watchers))

(def old-replica {:pairs {:a :b :b :c :c :a} :prepared {:d :a} :peers [:a :b :c]})

(let [new-replica (f old-replica 0)
      diff (rep-diff old-replica new-replica (:args entry))
      reactions (rep-reactions old-replica new-replica diff {:id :c})]
  (fact diff => {:observer :d :subject :a})
  (fact reactions => [{:fn :accept-join-cluster
                       :args {:accepted {:observer :d
                                         :subject :a}
                              :updated-watch {:observer :c
                                              :subject :d}}}])
  (fact (rep-reactions old-replica new-replica diff {:id :a}) => nil)
  (fact (rep-reactions old-replica new-replica diff {:id :b}) => nil)
  (fact (rep-reactions old-replica new-replica diff {:id :d}) => nil))
