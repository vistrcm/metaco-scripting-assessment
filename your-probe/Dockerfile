FROM clojure
COPY deps.edn /app/deps.edn
COPY /src /app/src
WORKDIR /app
RUN clj -P -M -m probe
ENTRYPOINT [ "clj" ]
CMD [ "-M", "-m", "probe" ]
