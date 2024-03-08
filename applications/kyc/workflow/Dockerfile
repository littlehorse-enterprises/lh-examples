FROM golang:1.22 AS base

FROM base AS build

WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download

COPY *.go ./
RUN CGO_ENABLED=0 GOOS=linux go build -o /workflow

FROM base
WORKDIR /
COPY --from=build /workflow /workflow
ENTRYPOINT [ "/workflow" ]
