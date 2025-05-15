"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { useRouter } from "next/navigation"
import { RefreshCw } from "lucide-react"
import Link from "next/link"

interface Role {
  id: number
  name: string
}

interface Module {
  id: number
  name: string
}

export default function RoleDetailsPage({ params }: { params: { id: string } }) {
  const router = useRouter()

  // If the ID is "create", redirect to the create page
  // This is a safeguard in case this component gets rendered for /roles/create
  useEffect(() => {
    if (params.id === "create") {
      router.replace("/roles/create")
    }
  }, [params.id, router])

  // If the ID is "create", show a loading state until the redirect happens
  if (params.id === "create") {
    return (
      <div className="container mx-auto py-12 flex justify-center">
        <RefreshCw className="h-8 w-8 animate-spin" />
      </div>
    )
  }

  const [role, setRole] = useState<Role | null>(null)
  const [modules, setModules] = useState<Module[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")

  useEffect(() => {
    const fetchRoleDetails = async () => {
      setLoading(true)
      try {
        // Check if the ID is a valid number
        const roleId = Number.parseInt(params.id)
        if (isNaN(roleId)) {
          throw new Error("Invalid role ID")
        }

        // First check if the API is available
        const roleResponse = await fetch(`/api/roles/${params.id}`, {
          // Add cache: 'no-store' to prevent caching issues
          cache: "no-store",
        })

        if (!roleResponse.ok) {
          const errorData = await roleResponse.json()
          throw new Error(errorData.message || "Failed to fetch role details")
        }

        const roleData = await roleResponse.json()
        setRole(roleData)

        // Fetch modules for this role
        const modulesResponse = await fetch(`/api/roles/modules`, {
          cache: "no-store",
        })

        if (!modulesResponse.ok) {
          throw new Error("Failed to fetch modules")
        }

        const modulesData = await modulesResponse.json()
        setModules(modulesData)
      } catch (error) {
        console.error("Error fetching role details:", error)
        setError(error instanceof Error ? error.message : "Failed to load role details")
      } finally {
        setLoading(false)
      }
    }

    fetchRoleDetails()
  }, [params.id])

  if (loading) {
    return (
      <div className="container mx-auto py-12 flex justify-center">
        <RefreshCw className="h-8 w-8 animate-spin" />
      </div>
    )
  }

  if (error || !role) {
    return (
      <div className="container mx-auto py-12">
        <div className="bg-red-50 text-red-600 p-4 rounded-md">{error || "Role not found"}</div>
        <Button onClick={() => router.push("/roles")} className="mt-4">
          Back to Roles
        </Button>
      </div>
    )
  }

  return (
    <div className="container mx-auto py-6 max-w-3xl">
      <div className="bg-white rounded-lg shadow-sm border">
        <div className="bg-blue-600 text-white p-4 rounded-t-lg">
          <h1 className="text-2xl font-bold">Role Details</h1>
        </div>

        <div className="p-6">
          <div className="border-b pb-4 mb-4">
            <div className="flex items-center">
              <span className="text-gray-600 font-medium w-32">Name:</span>
              <span className="text-blue-600 font-medium">{role.name}</span>
            </div>
          </div>

          <div className="border-b pb-4 mb-4">
            <div className="flex items-center">
              <span className="text-gray-600 font-medium w-32">Modules Count:</span>
              <span className="bg-blue-100 text-blue-600 px-2 py-1 rounded-md font-medium">{modules.length}</span>
            </div>
          </div>

          <div>
            <h2 className="text-xl font-bold mb-4">Modules</h2>
            <div className="border rounded-md max-h-80 overflow-y-auto">
              {modules.length === 0 ? (
                <p className="p-4 text-center text-gray-500">No modules assigned</p>
              ) : (
                modules.map((module) => (
                  <div key={module.id} className="p-4 border-b last:border-b-0">
                    {module.name}
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="mt-6 flex justify-end">
            <Button variant="outline" asChild>
              <Link href="/roles">Back to List</Link>
            </Button>
          </div>
        </div>
      </div>
    </div>
  )
}

